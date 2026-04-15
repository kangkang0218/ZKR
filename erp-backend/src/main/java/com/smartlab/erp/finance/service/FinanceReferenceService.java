package com.smartlab.erp.finance.service;

import com.smartlab.erp.entity.SysProject;
import com.smartlab.erp.entity.User;
import com.smartlab.erp.finance.entity.FinanceVentureProfile;
import com.smartlab.erp.finance.entity.FinanceWalletAccount;
import com.smartlab.erp.finance.repository.FinanceVentureProfileRepository;
import com.smartlab.erp.finance.repository.FinanceWalletAccountRepository;
import com.smartlab.erp.repository.SysProjectRepository;
import com.smartlab.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FinanceReferenceService {

    private final SysProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FinanceVentureProfileRepository ventureProfileRepository;
    private final FinanceWalletAccountRepository walletAccountRepository;

    @Transactional(readOnly = true)
    public SysProject getRequiredProject(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
    }

    @Transactional(readOnly = true)
    public User getRequiredUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    @Transactional(readOnly = true)
    public FinanceVentureProfile getRequiredVentureByLegacyId(Long legacyVentureId) {
        return ventureProfileRepository.findByLegacyVentureId(legacyVentureId)
                .orElseThrow(() -> new IllegalArgumentException("Finance venture not found: " + legacyVentureId));
    }

    @Transactional
    public FinanceWalletAccount getOrCreateWallet(String userId) {
        return walletAccountRepository.findByOwner_UserId(userId)
                .orElseGet(() -> walletAccountRepository.save(FinanceWalletAccount.builder()
                        .owner(getRequiredUser(userId))
                        .balance(BigDecimal.ZERO)
                        .totalDividendEarned(BigDecimal.ZERO)
                        .totalRoyaltyEarned(BigDecimal.ZERO)
                        .totalMiddlewareProfit(BigDecimal.ZERO)
                        .totalPromotionExpense(BigDecimal.ZERO)
                        .totalAdjustmentAmount(BigDecimal.ZERO)
                        .build()));
    }
}
