package com.smartlab.erp.finance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlab.erp.finance.dto.FinanceAiContextBlock;
import com.smartlab.erp.finance.dto.FinanceRagDataRow;
import com.smartlab.erp.finance.dto.FinanceRagPushResponse;
import com.smartlab.erp.finance.dto.FinanceRagQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FinanceExternalRagClient {

    private final ObjectMapper objectMapper;

    @Value("${finance.rag.enabled:true}")
    private boolean enabled;

    @Value("${finance.rag.base-url:http://finance-rag-api:8088}")
    private String baseUrl;

    @Value("${finance.rag.index-name:finance-rag-index}")
    private String indexName;

    @Value("${finance.rag.connect-timeout-seconds:5}")
    private long connectTimeoutSeconds;

    @Value("${finance.rag.request-timeout-seconds:120}")
    private long requestTimeoutSeconds;

    public boolean isEnabled() {
        return enabled;
    }

    public FinanceRagQueryResponse query(String prompt,
                                         int limit,
                                         List<FinanceAiContextBlock> contextBlocks,
                                         List<String> approvedSourceTypes) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("prompt", prompt);
        payload.put("limit", limit);
        payload.put("contextBlocks", contextBlocks);
        JsonNode body = exchange("/api/query", payload);
        return FinanceRagQueryResponse.builder()
                .answer(body.path("answer").asText(""))
                .readOnly(true)
                .approvedSourceTypes(approvedSourceTypes)
                .dataRows(parseRows(body.path("dataRows")))
                .contextBlocks(contextBlocks)
                .build();
    }

    public FinanceRagPushResponse push(List<FinanceAiContextBlock> contextBlocks) {
        Map<String, Object> payload = Map.of("contextBlocks", contextBlocks);
        JsonNode body = exchange("/api/index", payload);
        return FinanceRagPushResponse.builder()
                .indexName(body.path("indexName").asText(indexName))
                .status(body.path("status").asText("ACTIVE"))
                .documentCount(body.path("documentCount").asInt(contextBlocks.size()))
                .message(body.path("message").asText("Finance RAG index refreshed from current finance context"))
                .build();
    }

    private JsonNode exchange(String path, Map<String, Object> payload) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .setReadTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + path, new HttpEntity<>(payload, headers), String.class);
        try {
            return objectMapper.readTree(response.getBody() == null ? "{}" : response.getBody());
        } catch (Exception ex) {
            throw new IllegalStateException("finance rag response parse failed", ex);
        }
    }

    private List<FinanceRagDataRow> parseRows(JsonNode rowsNode) {
        List<FinanceRagDataRow> rows = new ArrayList<>();
        if (!rowsNode.isArray()) {
            return rows;
        }
        for (JsonNode rowNode : rowsNode) {
            rows.add(FinanceRagDataRow.builder()
                    .title(rowNode.path("title").asText("Finance Context"))
                    .snippet(rowNode.path("snippet").asText(""))
                    .sourceTable(rowNode.path("sourceTable").asText("finance_context"))
                    .sourceId(rowNode.path("sourceId").isMissingNode() || rowNode.path("sourceId").isNull() ? null : rowNode.path("sourceId").asLong())
                    .score(rowNode.path("score").asInt(0))
                    .build());
        }
        return rows;
    }
}
