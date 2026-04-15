import hashlib
import json
import os
import time
from typing import Any

import requests
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from qdrant_client import QdrantClient
from qdrant_client.http.models import Distance, PointStruct, VectorParams
from redis import Redis


def env(name: str, default: str) -> str:
    value = os.getenv(name)
    return value.strip() if value else default


QDRANT_URL = env("QDRANT_URL", "http://qdrant:6333")
REDIS_URL = env("REDIS_URL", "redis://redis:6379/0")
LLM_PROVIDER = env("LLM_PROVIDER", "ollama").lower()
EMBEDDING_PROVIDER = env("EMBEDDING_PROVIDER", LLM_PROVIDER).lower()
OLLAMA_BASE_URL = env("OLLAMA_BASE_URL", "http://host.docker.internal:11434")
OLLAMA_MODEL = env("OLLAMA_MODEL", "qwen3:8b")
OLLAMA_EMBED_MODEL = env("OLLAMA_EMBED_MODEL", "qwen3-embedding:0.6b")
OPENAI_BASE_URL = env("OPENAI_BASE_URL", "https://api.openai.com/v1")
OPENAI_API_KEY = env("OPENAI_API_KEY", "")
OPENAI_MODEL = env("OPENAI_MODEL", "gpt-4o-mini")
OPENAI_EMBED_MODEL = env("OPENAI_EMBED_MODEL", "text-embedding-3-small")
INDEX_NAME = env("INDEX_NAME", "finance-rag-index")
SEARCH_LIMIT = int(env("SEARCH_LIMIT", "5"))
REQUEST_TIMEOUT_SECONDS = float(env("REQUEST_TIMEOUT_SECONDS", "120"))

app = FastAPI(title="finance-rag-sidecar")
qdrant = QdrantClient(url=QDRANT_URL)
redis_client = Redis.from_url(REDIS_URL, decode_responses=False)
vector_size: int | None = None


class ContextBlock(BaseModel):
    title: str = ""
    content: str = ""
    sourceType: str | None = None
    sourceKey: str | None = None


class QueryRequest(BaseModel):
    prompt: str
    limit: int | None = Field(default=5, ge=1, le=20)
    contextBlocks: list[ContextBlock] = Field(default_factory=list)


class IndexRequest(BaseModel):
    contextBlocks: list[ContextBlock] = Field(default_factory=list)


def embedding_cache_key(text: str) -> str:
    provider_model = f"{EMBEDDING_PROVIDER}:{OPENAI_EMBED_MODEL if EMBEDDING_PROVIDER == 'openai' else OLLAMA_EMBED_MODEL}"
    payload = f"{provider_model}\n{text}"
    return "embed:" + hashlib.sha256(payload.encode("utf-8")).hexdigest()


def point_id(block: ContextBlock) -> int:
    raw = "|".join(
        [
            block.sourceType or "",
            block.sourceKey or "",
            block.title or "",
            block.content or "",
        ]
    )
    return int(hashlib.sha256(raw.encode("utf-8")).hexdigest()[:15], 16)


def normalize_text(block: ContextBlock) -> str:
    parts = [block.title.strip(), block.content.strip(), (block.sourceType or "").strip()]
    return "\n".join(part for part in parts if part)


def ensure_collection(size: int) -> None:
    global vector_size
    if vector_size == size:
        return
    collections = [item.name for item in qdrant.get_collections().collections]
    if INDEX_NAME not in collections:
        qdrant.create_collection(
            collection_name=INDEX_NAME,
            vectors_config=VectorParams(size=size, distance=Distance.COSINE),
        )
    vector_size = size


def reset_collection() -> None:
    global vector_size
    collections = [item.name for item in qdrant.get_collections().collections]
    if INDEX_NAME in collections:
        qdrant.delete_collection(collection_name=INDEX_NAME)
    vector_size = None


def embed_text(text: str) -> list[float]:
    key = embedding_cache_key(text)
    cached = redis_client.get(key)
    if cached:
        values = cached.decode("utf-8").split(",")
        return [float(value) for value in values if value]

    vector = request_embedding(text)
    if not isinstance(vector, list) or not vector:
        raise HTTPException(status_code=502, detail="embedding service returned no vector")
    redis_client.setex(key, 86400, ",".join(str(value) for value in vector).encode("utf-8"))
    return [float(value) for value in vector]


def request_embedding(text: str) -> list[float]:
    if EMBEDDING_PROVIDER == "openai":
        return request_openai_embedding(text)
    return request_ollama_embedding(text)


def request_ollama_embedding(text: str) -> list[float]:
    response = requests.post(
        f"{OLLAMA_BASE_URL}/api/embeddings",
        json={"model": OLLAMA_EMBED_MODEL, "prompt": text},
        timeout=REQUEST_TIMEOUT_SECONDS,
    )
    response.raise_for_status()
    return response.json().get("embedding") or []


def request_openai_embedding(text: str) -> list[float]:
    if not OPENAI_API_KEY:
        raise HTTPException(status_code=500, detail="OPENAI_API_KEY is required for openai embeddings")
    response = requests.post(
        f"{normalized_openai_base_url()}/embeddings",
        headers={
            "Authorization": f"Bearer {OPENAI_API_KEY}",
            "Content-Type": "application/json",
        },
        json={"model": OPENAI_EMBED_MODEL, "input": text},
        timeout=REQUEST_TIMEOUT_SECONDS,
    )
    response.raise_for_status()
    data = response.json().get("data") or []
    if not data:
        return []
    return data[0].get("embedding") or []


def upsert_blocks(blocks: list[ContextBlock]) -> int:
    if not blocks:
        return 0

    points: list[PointStruct] = []
    for block in blocks:
        text = normalize_text(block)
        if not text:
            continue
        vector = embed_text(text)
        ensure_collection(len(vector))
        payload = {
            "title": block.title,
            "content": block.content,
            "sourceType": block.sourceType,
            "sourceKey": block.sourceKey,
            "text": text,
            "indexedAt": int(time.time()),
        }
        points.append(PointStruct(id=point_id(block), vector=vector, payload=payload))

    if points:
        qdrant.upsert(collection_name=INDEX_NAME, points=points, wait=True)
    return len(points)


def search(prompt: str, limit: int) -> list[dict[str, Any]]:
    query_vector = embed_text(prompt.strip())
    ensure_collection(len(query_vector))
    results = qdrant.search(collection_name=INDEX_NAME, query_vector=query_vector, limit=limit)
    rows: list[dict[str, Any]] = []
    for item in results:
        payload = item.payload or {}
        source_id = payload.get("sourceKey")
        try:
            source_id = int(source_id) if source_id is not None and str(source_id).strip() else None
        except ValueError:
            source_id = None
        rows.append(
            {
                "title": payload.get("title") or "Finance Context",
                "snippet": payload.get("content") or payload.get("text") or "",
                "sourceTable": payload.get("sourceType") or "finance_context",
                "sourceId": source_id,
                "score": max(1, int(round(float(item.score or 0) * 100))),
            }
        )
    return rows


def build_answer(prompt: str, rows: list[dict[str, Any]]) -> str:
    if not rows:
        return (
            f"结论\n"
            f"- 未检索到与问题“{prompt.strip()}”直接相关的 ERP 业务上下文。\n\n"
            f"建议\n"
            f"- 请补充项目名、成员名、时间范围或业务动作后再试。"
        )

    context = "\n\n".join(
        f"[{index + 1}] {row['title']}\n{row['snippet']}" for index, row in enumerate(rows)
    )
    instruction = (
        "你是企业全局业务RAG助手，只能依据给定上下文回答，禁止编造。"
        "请严格使用中文并按下面结构输出，保留空行，便于前端直接展示：\n"
        "结论\n"
        "- 1到2条最直接的结论\n\n"
        "依据\n"
        "- 2到4条关键依据，每条尽量短，并引用上下文中的项目名、成员名、数量或时间\n\n"
        "建议\n"
        "- 1到3条下一步建议；如果没有明显建议，就写“暂无额外建议”\n\n"
        "禁止输出乱码、禁止长段落堆叠、禁止把三个部分写成一整段。"
    )
    answer = request_completion(
        f"{instruction}\n\n用户问题：{prompt.strip()}\n\n上下文：\n{context}"
    )
    if answer:
        return answer
    return "\n".join(f"- {row['title']}: {row['snippet']}" for row in rows)


def request_completion(prompt: str) -> str:
    if LLM_PROVIDER == "openai":
        return request_openai_completion(prompt)
    return request_ollama_completion(prompt)


def request_ollama_completion(prompt: str) -> str:
    response = requests.post(
        f"{OLLAMA_BASE_URL}/api/generate",
        json={
            "model": OLLAMA_MODEL,
            "prompt": prompt,
            "stream": False,
            "options": {"temperature": 0.1},
        },
        timeout=REQUEST_TIMEOUT_SECONDS,
    )
    response.raise_for_status()
    return (response.json().get("response") or "").strip()


def request_openai_completion(prompt: str) -> str:
    if not OPENAI_API_KEY:
        raise HTTPException(status_code=500, detail="OPENAI_API_KEY is required for openai chat completions")
    response = requests.post(
        f"{normalized_openai_base_url()}/chat/completions",
        headers={
            "Authorization": f"Bearer {OPENAI_API_KEY}",
            "Content-Type": "application/json",
        },
        json={
            "model": OPENAI_MODEL,
            "messages": [
                {"role": "system", "content": "你是企业全局业务RAG助手，只能依据给定上下文回答，禁止编造。"},
                {"role": "user", "content": prompt},
            ],
            "temperature": 0.1,
        },
        timeout=REQUEST_TIMEOUT_SECONDS,
    )
    response.raise_for_status()
    response.encoding = "utf-8"
    try:
        payload = json.loads(response.content.decode("utf-8"))
    except (UnicodeDecodeError, ValueError) as exc:
        preview = response.content[:300].decode("utf-8", errors="replace")
        raise HTTPException(status_code=502, detail=f"openai response was not valid utf-8 json: {preview}") from exc
    choices = payload.get("choices") or []
    if not choices:
        return ""
    message = choices[0].get("message") or {}
    return str(message.get("content") or "").strip()


def normalized_openai_base_url() -> str:
    base_url = OPENAI_BASE_URL.rstrip("/")
    return base_url if base_url.endswith("/v1") else f"{base_url}/v1"


@app.get("/health")
def health() -> dict[str, Any]:
    collections = [item.name for item in qdrant.get_collections().collections]
    return {
        "status": "ok",
        "indexName": INDEX_NAME,
        "collectionExists": INDEX_NAME in collections,
        "llmProvider": LLM_PROVIDER,
        "embeddingProvider": EMBEDDING_PROVIDER,
        "ollamaBaseUrl": OLLAMA_BASE_URL,
        "openaiBaseUrl": OPENAI_BASE_URL,
        "model": OPENAI_MODEL if LLM_PROVIDER == "openai" else OLLAMA_MODEL,
        "embeddingModel": OPENAI_EMBED_MODEL if EMBEDDING_PROVIDER == "openai" else OLLAMA_EMBED_MODEL,
    }


@app.post("/api/index")
def index_documents(request: IndexRequest) -> dict[str, Any]:
    reset_collection()
    count = upsert_blocks(request.contextBlocks)
    return {
        "indexName": INDEX_NAME,
        "status": "ACTIVE",
        "documentCount": count,
        "message": "Business RAG index refreshed from current ERP context",
    }


@app.post("/api/query")
def query_documents(request: QueryRequest) -> dict[str, Any]:
    prompt = request.prompt.strip()
    if not prompt:
        raise HTTPException(status_code=400, detail="prompt is required")
    upsert_blocks(request.contextBlocks)
    rows = search(prompt, request.limit or SEARCH_LIMIT)
    return {
        "answer": build_answer(prompt, rows),
        "dataRows": rows,
    }
