package com.smartlab.erp.finance.repository;

import com.smartlab.erp.finance.entity.FinanceExpenseSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceExpenseSubmissionRepository extends JpaRepository<FinanceExpenseSubmission, Long> {
    List<FinanceExpenseSubmission> findAllByOrderByCreatedAtDesc();
}
