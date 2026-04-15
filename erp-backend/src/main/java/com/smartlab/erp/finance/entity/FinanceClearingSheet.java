package com.smartlab.erp.finance.entity;

import com.smartlab.erp.entity.SysProject;
import com.smartlab.erp.finance.enums.FinanceClearingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "finance_clearing_sheet", indexes = {
        @Index(name = "idx_finance_clearing_project_month", columnList = "project_id,ledger_month")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceClearingSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private SysProject project;

    @Column(name = "ledger_month", nullable = false, length = 7)
    private String ledgerMonth;

    @Column(name = "final_revenue", precision = 15, scale = 2)
    private BigDecimal finalRevenue;

    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "net_profit", precision = 15, scale = 2)
    private BigDecimal netProfit;

    @Column(name = "middleware_fee", precision = 15, scale = 2)
    private BigDecimal middlewareFee;

    @Column(name = "carry_forward_loss", precision = 15, scale = 2)
    private BigDecimal carryForwardLoss;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FinanceClearingStatus status = FinanceClearingStatus.PENDING;

    @Column(name = "cleared_at")
    private Instant clearedAt;

    @Column(name = "cleared_by", length = 64)
    private String clearedBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
