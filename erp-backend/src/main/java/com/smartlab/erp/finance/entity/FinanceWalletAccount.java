package com.smartlab.erp.finance.entity;

import com.smartlab.erp.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "finance_wallet_account", indexes = {
        @Index(name = "idx_finance_wallet_user", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceWalletAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "total_dividend_earned", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalDividendEarned = BigDecimal.ZERO;

    @Column(name = "total_royalty_earned", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalRoyaltyEarned = BigDecimal.ZERO;

    @Column(name = "total_middleware_profit", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalMiddlewareProfit = BigDecimal.ZERO;

    @Column(name = "total_promotion_expense", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalPromotionExpense = BigDecimal.ZERO;

    @Column(name = "total_adjustment_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalAdjustmentAmount = BigDecimal.ZERO;

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
