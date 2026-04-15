package com.smartlab.erp.finance.entity;

import com.smartlab.erp.finance.enums.FinanceBatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "finance_cost_batch", indexes = {
        @Index(name = "idx_finance_cost_batch_month", columnList = "ledger_month")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceCostBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ledger_month", nullable = false, length = 7)
    private String ledgerMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FinanceBatchStatus status = FinanceBatchStatus.PENDING;

    @Column(name = "generated_record_count")
    @Builder.Default
    private Integer generatedRecordCount = 0;

    @Column(name = "operator_user_id", length = 64)
    private String operatorUserId;

    @Column(name = "batch_date")
    private LocalDate batchDate;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @PrePersist
    protected void onCreate() {
        if (batchDate == null) {
            batchDate = LocalDate.now();
        }
        if (startedAt == null) {
            startedAt = Instant.now();
        }
    }
}
