package com.smartlab.erp.finance.repository;

import com.smartlab.erp.finance.entity.FinanceKnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinanceKnowledgeDocumentRepository extends JpaRepository<FinanceKnowledgeDocument, Long> {
    List<FinanceKnowledgeDocument> findByActiveTrueOrderByUpdatedAtDesc();

    List<FinanceKnowledgeDocument> findBySourceTableAndSourceId(String sourceTable, Long sourceId);
}
