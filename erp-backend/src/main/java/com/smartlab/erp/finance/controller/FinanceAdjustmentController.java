package com.smartlab.erp.finance.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.smartlab.erp.finance.dto.*;
import com.smartlab.erp.finance.enums.FinanceAdjustmentDirection;
import com.smartlab.erp.finance.service.FinanceAdjustmentService;
import com.smartlab.erp.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/adjustment")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class FinanceAdjustmentController {

    private final FinanceAdjustmentService adjustmentService;

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<FinanceAdjustmentCreateResponse>> create(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody FinanceAdjustmentCreateRequest request) {
        String traceId = UUID.randomUUID().toString();
        try {
            return ResponseEntity.ok(FinanceApiResponse.success(
                    "Adjustment created",
                    adjustmentService.create(request, currentUser == null ? null : currentUser.getUserId()),
                    null,
                    traceId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(FinanceApiResponse.failure(ex.getMessage(), traceId));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinanceApiResponse<FinanceAdjustmentListResponse>> list(
            @RequestParam(required = false) String userId) {
        String traceId = UUID.randomUUID().toString();
        return ResponseEntity.ok(FinanceApiResponse.success(
                "Adjustment logs loaded",
                adjustmentService.list(userId),
                null,
                traceId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FinanceApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(FinanceApiResponse.failure(resolveValidationMessage(ex), traceId()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<FinanceApiResponse<Void>> handleUnreadablePayload(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(FinanceApiResponse.failure(resolvePayloadMessage(ex), traceId()));
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

    private String resolvePayloadMessage(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormat
                && invalidFormat.getTargetType() == FinanceAdjustmentDirection.class) {
            return "direction must be DEBIT or CREDIT";
        }
        return "Malformed request payload";
    }
}
