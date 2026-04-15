package com.smartlab.erp.finance.entity;

import com.smartlab.erp.entity.SysProject;
import com.smartlab.erp.entity.User;
import com.smartlab.erp.finance.enums.FinanceDividendStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "finance_dividend_sheet", indexes = {
        @Index(name = "idx_finance_dividend_project_status", columnList = "project_id,status"),
        @Index(name = "idx_finance_dividend_user_status", columnList = "user_id,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceDividendSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private SysProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ledger_month", length = 7)
    private String ledgerMonth;

    @Column(name = "fin_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "fin_dividend_ratio", precision = 6, scale = 4)
    private BigDecimal dividendRatio;

    @Column(name = "net_profit_snapshot", precision = 15, scale = 2)
    private BigDecimal netProfitSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FinanceDividendStatus status = FinanceDividendStatus.PENDING;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "confirmed_by", length = 64)
    private String confirmedBy;

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
