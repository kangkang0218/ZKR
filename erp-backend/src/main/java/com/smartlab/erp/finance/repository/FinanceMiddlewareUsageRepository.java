package com.smartlab.erp.finance.repository;

import com.smartlab.erp.finance.entity.FinanceMiddlewareUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;

public interface FinanceMiddlewareUsageRepository extends JpaRepository<FinanceMiddlewareUsage, Long> {
    List<FinanceMiddlewareUsage> findByLedgerMonth(String ledgerMonth);

    List<FinanceMiddlewareUsage> findByCallerProject_ProjectId(String projectId);

    List<FinanceMiddlewareUsage> findByCallerProject_ProjectIdAndLedgerMonthAndClearingSheetIsNull(String projectId, String ledgerMonth);

    List<FinanceMiddlewareUsage> findByMiddleware_IdIn(Collection<Long> middlewareIds);
}
