package com.smartlab.erp.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "finance_bank_balance_snapshot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceBankBalanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "operator", nullable = false, length = 100)
    private String operator;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "snapshot_at", nullable = false)
    private Instant snapshotAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (snapshotAt == null) {
            snapshotAt = now;
        }
        createdAt = now;
    }
}
