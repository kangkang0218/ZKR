package com.smartlab.erp.finance.controller;

import com.smartlab.erp.finance.dto.*;
import com.smartlab.erp.finance.enums.FinanceDividendStatus;
import com.smartlab.erp.finance.service.FinanceDividendService;
import com.smartlab.erp.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

@RestController
@RequestMapping("/api/dividend")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class FinanceDividendController {

    private final FinanceDividendService dividendService;

    @PostMapping("/prepare")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<FinanceDividendPrepareResponse>> prepare(
            @Valid @RequestBody FinanceDividendPrepareRequest request) {
        String traceId = UUID.randomUUID().toString();
        try {
            return ResponseEntity.ok(FinanceApiResponse.success(
                    "Dividend sheets prepared",
                    dividendService.prepare(request),
                    null,
                    traceId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(FinanceApiResponse.failure(ex.getMessage(), traceId));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<FinanceDividendListResponse>> list(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) FinanceDividendStatus status) {
        String traceId = UUID.randomUUID().toString();
        try {
            return ResponseEntity.ok(FinanceApiResponse.success(
                    "Dividend sheets loaded",
                    dividendService.list(projectId, status),
                    null,
                    traceId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(FinanceApiResponse.failure(ex.getMessage(), traceId));
        }
    }

    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<FinanceDividendConfirmResponse>> confirm(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody FinanceDividendConfirmRequest request) {
        String traceId = UUID.randomUUID().toString();
        try {
            return ResponseEntity.ok(FinanceApiResponse.success(
                    "Dividend sheets confirmed",
                    dividendService.confirm(request, currentUser == null ? null : currentUser.getUserId()),
                    null,
                    traceId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(FinanceApiResponse.failure(ex.getMessage(), traceId));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FinanceApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(FinanceApiResponse.failure(resolveValidationMessage(ex), traceId()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<FinanceApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(FinanceApiResponse.failure(ex.getParameterName() + " is required", traceId()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FinanceApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(FinanceApiResponse.failure(ex.getName() + " value is invalid", traceId()));
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
