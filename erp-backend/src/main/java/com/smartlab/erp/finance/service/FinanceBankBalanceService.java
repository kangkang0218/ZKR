package com.smartlab.erp.finance.service;

import com.smartlab.erp.finance.dto.FinanceBankBalanceRequest;
import com.smartlab.erp.finance.dto.FinanceMutationResult;
import com.smartlab.erp.finance.entity.FinanceBankBalanceSnapshot;
import com.smartlab.erp.finance.repository.FinanceBankBalanceSnapshotRepository;
import com.smartlab.erp.finance.support.FinanceAmounts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class FinanceBankBalanceService {

    private final FinanceBankBalanceSnapshotRepository bankBalanceSnapshotRepository;

    @Transactional
    public FinanceMutationResult recordBankBalance(FinanceBankBalanceRequest request) {
        FinanceBankBalanceSnapshot snapshot = bankBalanceSnapshotRepository.save(FinanceBankBalanceSnapshot.builder()
                .balance(FinanceAmounts.scale(request.getBalance()))
                .operator(request.getOperator().trim())
                .remark(request.getRemark())
                .snapshotAt(Instant.now())
                .build());

        return FinanceMutationResult.builder()
                .id(String.valueOf(snapshot.getId()))
                .message("bank balance snapshot recorded")
                .build();
    }
}
