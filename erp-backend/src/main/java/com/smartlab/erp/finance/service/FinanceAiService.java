package com.smartlab.erp.finance.service;

import com.smartlab.erp.finance.dto.FinanceAiChatRequest;
import com.smartlab.erp.finance.dto.FinanceAiChatResponse;
import com.smartlab.erp.finance.dto.FinanceAiContextBlock;
import com.smartlab.erp.finance.dto.FinanceRagQueryRequest;
import com.smartlab.erp.finance.dto.FinanceRagQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceAiService {

    private final FinanceAiContextService financeAiContextService;
    private final FinanceRagService financeRagService;
    private final FinanceAiGateway financeAiGateway;

    @Transactional(readOnly = true)
    public FinanceAiChatResponse chat(FinanceAiChatRequest request) {
        validateMessage(request.getMessage());
        try {
            FinanceRagQueryResponse ragResponse = financeRagService.query(FinanceRagQueryRequest.builder()
                    .prompt(request.getMessage())
                    .limit(5)
                    .build());
            return FinanceAiChatResponse.builder()
                    .answer(ragResponse.getAnswer())
                    .provider("RAG")
                    .attemptedProvider("RAG")
                    .errorMessage(null)
                    .fallbackUsed(false)
                    .fallbackProvider(null)
                    .fallbackReason(null)
                    .readOnly(ragResponse.isReadOnly())
                    .streaming(false)
                    .approvedSourceTypes(ragResponse.getApprovedSourceTypes())
                    .contextBlocks(ragResponse.getContextBlocks())
                    .dataRows(ragResponse.getDataRows())
                    .build();
        } catch (RuntimeException ex) {
            List<FinanceAiContextBlock> contextBlocks = financeAiContextService.buildContextBlocks();
            List<String> approvedSourceTypes = financeAiContextService.approvedSourceTypes();
            return FinanceAiChatResponse.builder()
                    .answer(financeAiGateway.generateAnswer(request.getMessage(), contextBlocks))
                    .provider("AI")
                    .attemptedProvider("RAG")
                    .errorMessage(ex.getMessage())
                    .fallbackUsed(true)
                    .fallbackProvider("AI")
                    .fallbackReason(ex.getMessage())
                    .readOnly(true)
                    .streaming(false)
                    .approvedSourceTypes(approvedSourceTypes)
                    .contextBlocks(contextBlocks)
                    .dataRows(List.of())
                    .build();
        }
    }

    private void validateMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message is required");
        }
    }
}
