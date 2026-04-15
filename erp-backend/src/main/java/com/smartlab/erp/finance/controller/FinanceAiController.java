package com.smartlab.erp.finance.controller;

import com.smartlab.erp.finance.dto.FinanceAiChatRequest;
import com.smartlab.erp.finance.dto.FinanceAiChatResponse;
import com.smartlab.erp.finance.dto.FinanceApiResponse;
import com.smartlab.erp.finance.dto.FinanceRagPushResponse;
import com.smartlab.erp.finance.dto.FinanceRagQueryRequest;
import com.smartlab.erp.finance.dto.FinanceRagQueryResponse;
import com.smartlab.erp.finance.service.FinanceAiService;
import com.smartlab.erp.finance.service.FinanceRagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FinanceAiController {

    private final FinanceAiService financeAiService;
    private final FinanceRagService financeRagService;

    @PostMapping("/api/ai/chat")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<FinanceAiChatResponse>> chat(@Valid @RequestBody FinanceAiChatRequest request) {
        String traceId = traceId();
        return ResponseEntity.ok(FinanceApiResponse.success(
                "finance ai chat completed",
                financeAiService.chat(request),
                null,
                traceId));
    }

    @PostMapping("/api/rag/query")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<FinanceRagQueryResponse>> query(@Valid @RequestBody FinanceRagQueryRequest request) {
        String traceId = traceId();
        return ResponseEntity.ok(FinanceApiResponse.success(
                "finance rag query completed",
                financeRagService.query(request),
                null,
                traceId));
    }

    @PostMapping("/api/rag/push")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<FinanceRagPushResponse>> push() {
        String traceId = traceId();
        return ResponseEntity.status(HttpStatus.CREATED).body(FinanceApiResponse.success(
                "finance rag index refreshed",
                financeRagService.push(),
                null,
                traceId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<FinanceApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        String traceId = traceId();
        return ResponseEntity.badRequest().body(FinanceApiResponse.failure(ex.getMessage(), traceId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FinanceApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(FinanceApiResponse.failure(resolveValidationMessage(ex), traceId()));
    }

    private String resolveValidationMessage(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "Validation failed" : error.getDefaultMessage())
                .orElse("Validation failed");
    }

    private String traceId() {
        return UUID.randomUUID().toString();
    }
}
