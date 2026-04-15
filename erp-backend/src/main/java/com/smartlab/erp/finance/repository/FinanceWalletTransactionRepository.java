package com.smartlab.erp.finance.repository;

import com.smartlab.erp.finance.entity.FinanceWalletTransaction;
import com.smartlab.erp.finance.enums.FinanceWalletTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceWalletTransactionRepository extends JpaRepository<FinanceWalletTransaction, Long> {
    List<FinanceWalletTransaction> findTop100ByWallet_IdOrderByIdDesc(Long walletId);

    List<FinanceWalletTransaction> findByTransactionType(FinanceWalletTransactionType transactionType);
}
