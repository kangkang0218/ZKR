package com.smartlab.erp.finance.repository;

import com.smartlab.erp.finance.entity.FinanceBankBalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinanceBankBalanceSnapshotRepository extends JpaRepository<FinanceBankBalanceSnapshot, Long> {
    Optional<FinanceBankBalanceSnapshot> findTopByOrderBySnapshotAtDesc();
}
