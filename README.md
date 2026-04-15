# ZKR ERP System

容器化部署的 ERP 系统，包含前端（Vue 3）、后端（Spring Boot）和 RAG 服务（Python）。

## 容器架构

| 服务 | 技术栈 | 端口 |
|------|--------|------|
| `erp-backend` | Spring Boot (Java 17) | 8080 |
| `lab-erp-demo` | Vue 3 + Nginx | 80 |
| `rag-service` | Python FastAPI + Qdrant | 8090 |
| `postgres` | PostgreSQL 16 | 5432 |
| `redis` | Redis 7.4 | 6379 |
| `qdrant` | Qdrant vector DB | 6333 |

## 快速开始

### 1. 环境要求

- Docker & Docker Compose
- 本地镜像仓库 `127.0.0.1:5555`（用于 `docker push/pull`）

### 2. 配置环境变量

```bash
cp .env.example .env
# 编辑 .env 填入实际值
```

关键配置项：
- `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD`
- `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD`
- `AUTH_PROVISION_ADMIN_*`（管理员Provisioning）
- `FINANCE_RAG_*`（RAG 服务相关）

### 3. 启动全部服务

```bash
docker compose up -d
```

### 4. 查看状态

```bash
docker compose ps
docker inspect zkr-lab-erp-demo --format '{{.Config.Image}}'
docker inspect zkr-erp-backend --format '{{.Config.Image}}'
```

## 构建与发布新版本

### 查询下一版本号

```bash
# 前端
python3 scripts/next_image_version.py 127.0.0.1:5555 zhangqi_frontend

# 后端
python3 scripts/next_image_version.py 127.0.0.1:5555 zhangqi_backend
```

### 构建前端镜像

```bash
docker build --build-arg APP_VERSION=<version> \
  -t 127.0.0.1:5555/zhangqi_frontend:<version> \
  -t 127.0.0.1:5555/zhangqi_frontend:latest \
  ./lab-erp-demo

docker push 127.0.0.1:5555/zhangqi_frontend:<version>
docker push 127.0.0.1:5555/zhangqi_frontend:latest
```

### 构建后端镜像

```bash
docker build --build-arg APP_VERSION=<version> \
  -t 127.0.0.1:5555/zhangqi_backend:<version> \
  -t 127.0.0.1:5555/zhangqi_backend:latest \
  ./erp-backend

docker push 127.0.0.1:5555/zhangqi_backend:<version>
docker push 127.0.0.1:5555/zhangqi_backend:latest
```

### 更新 docker-compose.yml

将 `docker-compose.yml` 中的镜像 tag 改为新版本号，然后重新部署：

```bash
docker compose up -d erp-backend lab-erp-demo
```

## 版本回滚

将 `docker-compose.yml` 中对应服务的镜像 tag 改回上一个稳定版本，执行：

```bash
docker compose up -d <service>
```

## 访问地址

| 入口 | 地址 |
|------|------|
| ERP 登录 | `http://<host>:8080/erp-login` |
| Finance 登录 | `http://<host>:8080/login` |
| 本地仓库 API | `http://127.0.0.1:5555/v2/` |

## 目录结构

```
ZKR/
├── docker-compose.yml      # 容器编排
├── erp-backend/            # Spring Boot 后端
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/     # Java 源码
├── lab-erp-demo/           # Vue 3 前端
│   ├── Dockerfile
│   ├── package.json
│   └── src/               # Vue 源码
└── rag-service/            # Python RAG 服务
    ├── Dockerfile
    ├── app.py
    └── requirements.txt
```
