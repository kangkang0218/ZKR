package com.smartlab.erp.finance.entity;

import com.smartlab.erp.entity.SysProject;
import com.smartlab.erp.finance.enums.FinanceCashFlowDirection;
import com.smartlab.erp.finance.enums.FinanceWalletTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "finance_wallet_transaction", indexes = {
        @Index(name = "idx_finance_wallet_transaction_wallet", columnList = "wallet_id"),
        @Index(name = "idx_finance_wallet_transaction_project", columnList = "project_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceWalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private FinanceWalletAccount wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private FinanceWalletTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "cash_flow_direction", nullable = false, length = 10)
    private FinanceCashFlowDirection cashFlowDirection;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private SysProject project;

    @Column(name = "source_table", length = 100)
    private String sourceTable;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
