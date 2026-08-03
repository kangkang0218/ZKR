package com.smartlab.erp.finance.repository;

import com.smartlab.erp.finance.entity.FinanceReportPrompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceReportPromptRepository extends JpaRepository<FinanceReportPrompt, Long> {
    List<FinanceReportPrompt> findAllByOrderByCreatedAtDesc();
}
