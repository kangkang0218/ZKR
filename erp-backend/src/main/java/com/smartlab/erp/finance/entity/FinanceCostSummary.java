package com.smartlab.erp.finance.entity;

import com.smartlab.erp.entity.SysProject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "finance_cost_summary", indexes = {
        @Index(name = "idx_finance_cost_summary_project_month", columnList = "project_id,ledger_month", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceCostSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private FinanceCostBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private SysProject project;

    @Column(name = "ledger_month", nullable = false, length = 7)
    private String ledgerMonth;

    @Column(name = "total_labor_cost", precision = 15, scale = 2)
    private BigDecimal totalLaborCost;

    @Column(name = "total_middleware_fee", precision = 15, scale = 2)
    private BigDecimal totalMiddlewareFee;

    @Column(name = "total_settlement_cost", precision = 15, scale = 2)
    private BigDecimal totalSettlementCost;

    @Column(name = "entry_count")
    private Integer entryCount;
}
