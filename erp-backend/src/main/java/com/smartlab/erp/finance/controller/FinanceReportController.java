package com.smartlab.erp.finance.controller;

import com.smartlab.erp.finance.dto.FinanceApiResponse;
import com.smartlab.erp.finance.dto.ReportGenerateRequest;
import com.smartlab.erp.finance.dto.ReportGenerateResponse;
import com.smartlab.erp.finance.dto.ReportPromptDto;
import com.smartlab.erp.finance.service.FinanceReportService;
import com.smartlab.erp.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finance/report")
@RequiredArgsConstructor
public class FinanceReportController {

    private final FinanceReportService reportService;

    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<ReportGenerateResponse>> generate(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ReportGenerateRequest request) {
        String traceId = traceId();
        try {
            return ResponseEntity.ok(FinanceApiResponse.success(
                    "报表生成完成",
                    reportService.generate(request,
                            currentUser == null ? null : currentUser.getUserId(),
                            currentUser == null ? null : currentUser.getName()),
                    null,
                    traceId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(FinanceApiResponse.failure(ex.getMessage(), traceId));
        }
    }

    @GetMapping("/prompts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<List<ReportPromptDto>>> prompts() {
        String traceId = traceId();
        return ResponseEntity.ok(FinanceApiResponse.success(
                "历史报表描述加载完成",
                reportService.listPrompts(),
                null,
                traceId));
    }

    @DeleteMapping("/prompts/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<Void>> deletePrompt(@PathVariable Long id) {
        String traceId = traceId();
        try {
            reportService.deletePrompt(id);
            return ResponseEntity.ok(FinanceApiResponse.success("报表描述已删除", null, null, traceId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(FinanceApiResponse.failure(ex.getMessage(), traceId));
        }
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
