package com.smartlab.erp.finance.controller;

import com.smartlab.erp.finance.dto.FinanceApiResponse;
import com.smartlab.erp.finance.dto.FinanceBankBalanceRequest;
import com.smartlab.erp.finance.dto.FinanceMutationResult;
import com.smartlab.erp.finance.service.FinanceBankBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceReportingController {

    private final FinanceBankBalanceService financeBankBalanceService;

    @PostMapping("/bank_balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<FinanceMutationResult>> recordBankBalance(
            @Valid @RequestBody FinanceBankBalanceRequest request) {
        String traceId = UUID.randomUUID().toString();
        FinanceMutationResult result = financeBankBalanceService.recordBankBalance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FinanceApiResponse.success("bank balance snapshot recorded", result, null, traceId));
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
