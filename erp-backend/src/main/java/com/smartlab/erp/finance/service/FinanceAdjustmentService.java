package com.smartlab.erp.finance.service;

import com.smartlab.erp.entity.User;
import com.smartlab.erp.finance.dto.*;
import com.smartlab.erp.finance.entity.FinanceAdjustmentLog;
import com.smartlab.erp.finance.entity.FinanceWalletAccount;
import com.smartlab.erp.finance.entity.FinanceWalletTransaction;
import com.smartlab.erp.finance.enums.FinanceAdjustmentDirection;
import com.smartlab.erp.finance.enums.FinanceCashFlowDirection;
import com.smartlab.erp.finance.enums.FinanceWalletTransactionType;
import com.smartlab.erp.finance.repository.FinanceAdjustmentLogRepository;
import com.smartlab.erp.finance.repository.FinanceWalletAccountRepository;
import com.smartlab.erp.finance.repository.FinanceWalletTransactionRepository;
import com.smartlab.erp.finance.support.FinanceAmounts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceAdjustmentService {

    private final FinanceReferenceService referenceService;
    private final FinanceAdjustmentLogRepository adjustmentLogRepository;
    private final FinanceWalletAccountRepository walletAccountRepository;
    private final FinanceWalletTransactionRepository walletTransactionRepository;

    @Transactional
    public FinanceAdjustmentCreateResponse create(FinanceAdjustmentCreateRequest request, String fallbackOperator) {
        validateRequest(request);
        User user = referenceService.getRequiredUser(request.getUserId());
        FinanceWalletAccount wallet = referenceService.getOrCreateWallet(user.getUserId());
        String operator = resolveOperator(request.getOperator(), fallbackOperator);
        BigDecimal signedAmount = resolveSignedAmount(request.getDirection(), request.getAmount());
        BigDecimal newBalance = FinanceAmounts.add(wallet.getBalance(), signedAmount);
        wallet.setBalance(newBalance);
        wallet.setTotalAdjustmentAmount(FinanceAmounts.add(wallet.getTotalAdjustmentAmount(), signedAmount));
        walletAccountRepository.save(wallet);

        FinanceAdjustmentLog log = adjustmentLogRepository.save(FinanceAdjustmentLog.builder()
                .wallet(wallet)
                .user(user)
                .direction(request.getDirection())
                .amount(FinanceAmounts.scale(request.getAmount()))
                .reason(resolveRemark(request))
                .sourceTable(request.getSubject().trim())
                .sourceId(request.getSourceId())
                .refDocNo(normalizeBlank(request.getRefDocNo()))
                .createdBy(operator)
                .build());

        walletTransactionRepository.save(FinanceWalletTransaction.builder()
                .wallet(wallet)
                .transactionType(FinanceWalletTransactionType.ADJUSTMENT)
                .cashFlowDirection(resolveCashFlowDirection(request.getDirection()))
                .amount(FinanceAmounts.scale(request.getAmount()))
                .balanceAfter(newBalance)
                .sourceTable("finance_adjustment_log")
                .sourceId(log.getId())
                .remark(request.getSubject().trim())
                .build());

        return FinanceAdjustmentCreateResponse.builder()
                .adjustment(FinanceMutationResult.builder()
                        .id(String.valueOf(log.getId()))
                        .message("Adjustment recorded")
                        .build())
                .user(toUserRef(user))
                .walletBalance(newBalance)
                .netAdjustment(wallet.getTotalAdjustmentAmount())
                .build();
    }

    @Transactional(readOnly = true)
    public FinanceAdjustmentListResponse list(String userId) {
        List<FinanceAdjustmentLog> logs = (userId == null || userId.isBlank())
                ? adjustmentLogRepository.findTop100ByOrderByIdDesc()
                : adjustmentLogRepository.findTop100ByUser_UserIdOrderByIdDesc(userId);

        List<FinanceAdjustmentItemView> items = logs.stream().map(this::toAdjustmentItemView).toList();
        BigDecimal debitTotal = logs.stream()
                .filter(log -> log.getDirection() == FinanceAdjustmentDirection.DEBIT)
                .map(FinanceAdjustmentLog::getAmount)
                .reduce(BigDecimal.ZERO, FinanceAmounts::add);
        BigDecimal creditTotal = logs.stream()
                .filter(log -> log.getDirection() == FinanceAdjustmentDirection.CREDIT)
                .map(FinanceAdjustmentLog::getAmount)
                .reduce(BigDecimal.ZERO, FinanceAmounts::add);

        return FinanceAdjustmentListResponse.builder()
                .items(items)
                .totalCount(items.size())
                .debitTotal(debitTotal)
                .creditTotal(creditTotal)
                .netAdjustment(FinanceAmounts.subtract(debitTotal, creditTotal))
                .build();
    }

    private void validateRequest(FinanceAdjustmentCreateRequest request) {
        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new IllegalArgumentException("Adjustment subject is required");
        }
        if (request.getDirection() == null) {
            throw new IllegalArgumentException("Adjustment direction is required");
        }
        if (!isSupportedDirection(request.getDirection())) {
            throw new IllegalArgumentException("Adjustment direction must be DEBIT or CREDIT");
        }
        if (request.getAmount() == null || FinanceAmounts.scale(request.getAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Adjustment amount must be greater than 0");
        }
    }

    private BigDecimal resolveSignedAmount(FinanceAdjustmentDirection direction, BigDecimal amount) {
        BigDecimal scaledAmount = FinanceAmounts.scale(amount);
        return direction == FinanceAdjustmentDirection.DEBIT ? scaledAmount : scaledAmount.negate();
    }

    private boolean isSupportedDirection(FinanceAdjustmentDirection direction) {
        return direction == FinanceAdjustmentDirection.DEBIT || direction == FinanceAdjustmentDirection.CREDIT;
    }

    private FinanceCashFlowDirection resolveCashFlowDirection(FinanceAdjustmentDirection direction) {
        return direction == FinanceAdjustmentDirection.DEBIT ? FinanceCashFlowDirection.IN : FinanceCashFlowDirection.OUT;
    }

    private FinanceAdjustmentItemView toAdjustmentItemView(FinanceAdjustmentLog log) {
        return FinanceAdjustmentItemView.builder()
                .adjustmentId(log.getId())
                .user(toUserRef(log.getUser()))
                .subject(log.getSourceTable())
                .direction(log.getDirection())
                .amount(FinanceAmounts.scale(log.getAmount()))
                .remark(log.getReason())
                .refDocNo(log.getRefDocNo())
                .audit(FinanceAuditRef.builder()
                        .sourceTable(log.getSourceTable())
                        .sourceId(log.getSourceId())
                        .build())
                .operator(log.getCreatedBy())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private FinanceUserRef toUserRef(User user) {
        return FinanceUserRef.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    private String resolveOperator(String requestOperator, String fallbackOperator) {
        if (requestOperator != null && !requestOperator.isBlank()) {
            return requestOperator;
        }
        if (fallbackOperator != null && !fallbackOperator.isBlank()) {
            return fallbackOperator;
        }
        throw new IllegalArgumentException("Operator is required for manual adjustments");
    }

    private String resolveRemark(FinanceAdjustmentCreateRequest request) {
        return normalizeBlank(request.getRemark()) != null ? request.getRemark().trim() : normalizeBlank(request.getReason());
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
