package com.smartlab.erp.finance.repository;

import com.smartlab.erp.finance.entity.FinanceAdjustmentLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceAdjustmentLogRepository extends JpaRepository<FinanceAdjustmentLog, Long> {
    List<FinanceAdjustmentLog> findTop100ByOrderByIdDesc();

    List<FinanceAdjustmentLog> findTop100ByUser_UserIdOrderByIdDesc(String userId);
}
