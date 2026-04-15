package com.smartlab.erp.finance.repository;

import com.smartlab.erp.enums.AccountDomain;
import com.smartlab.erp.finance.entity.FinanceWalletAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinanceWalletAccountRepository extends JpaRepository<FinanceWalletAccount, Long> {
    Optional<FinanceWalletAccount> findByOwner_UserId(String userId);

    List<FinanceWalletAccount> findAllByOwner_AccountDomain(AccountDomain accountDomain);
}
