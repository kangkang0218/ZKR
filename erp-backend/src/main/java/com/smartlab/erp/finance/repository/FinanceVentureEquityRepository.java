package com.smartlab.erp.finance.repository;

import com.smartlab.erp.finance.entity.FinanceVentureEquity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceVentureEquityRepository extends JpaRepository<FinanceVentureEquity, Long> {
    List<FinanceVentureEquity> findByProject_ProjectIdAndActiveTrue(String projectId);

    List<FinanceVentureEquity> findByUser_UserIdAndActiveTrue(String userId);
}
