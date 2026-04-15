package com.smartlab.erp.finance.service;

import com.smartlab.erp.finance.dto.FinanceAiContextBlock;
import com.smartlab.erp.finance.dto.FinanceRagDataRow;
import com.smartlab.erp.finance.dto.FinanceRagPushResponse;
import com.smartlab.erp.finance.dto.FinanceRagQueryRequest;
import com.smartlab.erp.finance.dto.FinanceRagQueryResponse;
import com.smartlab.erp.finance.entity.FinanceKnowledgeDocument;
import com.smartlab.erp.finance.repository.FinanceKnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FinanceRagService {

    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^a-z0-9]+");
    private static final String INDEX_NAME = "finance-rag-index";

    private final FinanceAiContextService financeAiContextService;
    private final FinanceKnowledgeDocumentRepository knowledgeDocumentRepository;
    private final FinanceExternalRagClient financeExternalRagClient;

    @Transactional(readOnly = true)
    public FinanceRagQueryResponse query(FinanceRagQueryRequest request) {
        validatePrompt(request.getPrompt());
        int limit = request.getLimit() == null || request.getLimit() <= 0 ? 5 : Math.min(request.getLimit(), 20);
        List<FinanceAiContextBlock> contextBlocks = resolveContextBlocks();
        if (financeExternalRagClient.isEnabled()) {
            try {
                return financeExternalRagClient.query(
                        request.getPrompt(),
                        limit,
                        contextBlocks,
                        financeAiContextService.approvedSourceTypes());
            } catch (RuntimeException ex) {
                // fall back to local keyword retrieval
            }
        }
        List<FinanceRagDataRow> rows = contextBlocks.stream()
                .map(this::toDocument)
                .map(document -> toRow(document, request.getPrompt()))
                .filter(row -> row.getScore() > 0)
                .sorted(Comparator.comparingInt(FinanceRagDataRow::getScore).reversed().thenComparing(FinanceRagDataRow::getTitle))
                .limit(limit)
                .toList();
        return FinanceRagQueryResponse.builder()
                .answer(buildAnswer(request.getPrompt(), rows))
                .readOnly(true)
                .approvedSourceTypes(financeAiContextService.approvedSourceTypes())
                .dataRows(rows)
                .contextBlocks(contextBlocks)
                .build();
    }

    private List<FinanceAiContextBlock> resolveContextBlocks() {
        try {
            return financeAiContextService.buildContextBlocks();
        } catch (RuntimeException ex) {
            return knowledgeDocumentRepository.findByActiveTrueOrderByUpdatedAtDesc().stream()
                    .filter(document -> financeAiContextService.approvedSourceTypes().contains(document.getSourceTable()))
                    .map(document -> FinanceAiContextBlock.builder()
                            .title(document.getTopic())
                            .content(document.getContent())
                            .sourceType(document.getSourceTable())
                            .sourceKey(document.getSourceId() == null ? null : String.valueOf(document.getSourceId()))
                            .build())
                    .toList();
        }
    }

    @Transactional
    public FinanceRagPushResponse push() {
        List<FinanceAiContextBlock> contextBlocks = financeAiContextService.buildContextBlocks();
        List<FinanceKnowledgeDocument> existing = knowledgeDocumentRepository.findByActiveTrueOrderByUpdatedAtDesc();
        if (!existing.isEmpty()) {
            existing.forEach(document -> document.setActive(Boolean.FALSE));
            knowledgeDocumentRepository.saveAll(existing);
        }

        List<FinanceKnowledgeDocument> refreshed = contextBlocks.stream()
                .map(this::toDocument)
                .toList();
        knowledgeDocumentRepository.saveAll(refreshed);
        if (financeExternalRagClient.isEnabled()) {
            try {
                return financeExternalRagClient.push(contextBlocks);
            } catch (RuntimeException ex) {
                // preserve local snapshot success even if external index refresh fails
            }
        }
        return FinanceRagPushResponse.builder()
                .indexName(INDEX_NAME)
                .status("ACTIVE")
                .documentCount(refreshed.size())
                .message("Finance RAG index refreshed from current finance context")
                .build();
    }

    private FinanceKnowledgeDocument toDocument(FinanceAiContextBlock block) {
        return FinanceKnowledgeDocument.builder()
                .topic(block.getTitle())
                .sourceTable(block.getSourceType())
                .sourceId(parseLong(block.getSourceKey()))
                .content(block.getContent())
                .embeddingRef(INDEX_NAME + ":" + normalize(block.getTitle()).replace(' ', '-'))
                .active(Boolean.TRUE)
                .build();
    }

    private FinanceRagDataRow toRow(FinanceKnowledgeDocument document, String prompt) {
        return FinanceRagDataRow.builder()
                .title(document.getTopic())
                .snippet(document.getContent())
                .sourceTable(document.getSourceTable())
                .sourceId(document.getSourceId())
                .score(score(prompt, document))
                .build();
    }

    private String buildAnswer(String prompt, List<FinanceRagDataRow> rows) {
        if (rows.isEmpty()) {
            return "未检索到与问题“" + prompt.trim() + "”直接相关的 ERP 业务上下文，请尝试补充项目名、成员名或时间范围。";
        }
        List<String> lines = new ArrayList<>();
        for (FinanceRagDataRow row : rows) {
            lines.add(row.getTitle() + ": " + row.getSnippet());
        }
        return "ERP 业务检索命中 " + rows.size() + " 条上下文：" + String.join(" | ", lines);
    }

    private int score(String prompt, FinanceKnowledgeDocument document) {
        String normalizedPrompt = normalize(prompt);
        String haystack = normalize(document.getTopic()) + " " + normalize(document.getContent()) + " " + normalize(document.getSourceTable());
        int score = haystack.contains(normalizedPrompt) ? 5 : 0;
        for (String token : TOKEN_SPLIT.split(normalizedPrompt)) {
            if (!token.isBlank() && haystack.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt is required");
        }
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
