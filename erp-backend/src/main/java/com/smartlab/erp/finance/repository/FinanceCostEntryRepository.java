package com.smartlab.erp.finance.repository;

import com.smartlab.erp.finance.entity.FinanceCostEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceCostEntryRepository extends JpaRepository<FinanceCostEntry, Long> {
    List<FinanceCostEntry> findByBatch_Id(Long batchId);

    List<FinanceCostEntry> findByProject_ProjectIdAndLedgerMonth(String projectId, String ledgerMonth);

    void deleteByProject_ProjectIdAndLedgerMonth(String projectId, String ledgerMonth);
}
