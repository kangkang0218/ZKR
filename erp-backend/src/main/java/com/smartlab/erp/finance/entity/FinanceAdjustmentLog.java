package com.smartlab.erp.finance.entity;

import com.smartlab.erp.entity.User;
import com.smartlab.erp.finance.enums.FinanceAdjustmentDirection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "finance_adjustment_log", indexes = {
        @Index(name = "idx_finance_adjustment_user_created", columnList = "user_id,created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceAdjustmentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private FinanceWalletAccount wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private FinanceAdjustmentDirection direction;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "source_table", length = 100)
    private String sourceTable;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "ref_doc_no", length = 100)
    private String refDocNo;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
