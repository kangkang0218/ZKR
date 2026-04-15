package com.smartlab.erp.finance.entity;

import com.smartlab.erp.entity.MiddlewareAsset;
import com.smartlab.erp.entity.SysProject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "finance_middleware_usage", indexes = {
        @Index(name = "idx_finance_middleware_usage_month", columnList = "ledger_month")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceMiddlewareUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "middleware_id", nullable = false)
    private MiddlewareAsset middleware;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_project_id", nullable = false)
    private SysProject callerProject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_project_id", nullable = false)
    private SysProject sourceProject;

    @Column(name = "royalty_fee", nullable = false, precision = 15, scale = 2)
    private BigDecimal royaltyFee;

    @Column(name = "ledger_month", nullable = false, length = 7)
    private String ledgerMonth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clearing_sheet_id")
    private FinanceClearingSheet clearingSheet;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
