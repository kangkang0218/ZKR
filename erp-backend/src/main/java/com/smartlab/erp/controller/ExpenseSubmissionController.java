package com.smartlab.erp.controller;

import com.smartlab.erp.finance.dto.FinanceExpenseSubmissionCreateRequest;
import com.smartlab.erp.finance.entity.FinanceExpenseSubmission;
import com.smartlab.erp.finance.service.FinanceExpenseSubmissionService;
import com.smartlab.erp.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class ExpenseSubmissionController {

    private final FinanceExpenseSubmissionService financeExpenseSubmissionService;

    @PostMapping(value = "/personal-procurement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> submitPersonalProcurement(
            @Valid @ModelAttribute FinanceExpenseSubmissionCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        FinanceExpenseSubmission submission = financeExpenseSubmissionService.submitPersonalProcurement(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", submission.getId(),
                "status", submission.getStatus().name(),
                "type", submission.getSubmissionType().name(),
                "message", "个人采购申请已提交至财务系统"
        ));
    }
}
