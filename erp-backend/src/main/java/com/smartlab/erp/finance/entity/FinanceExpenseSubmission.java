package com.smartlab.erp.finance.entity;

import com.smartlab.erp.finance.enums.FinanceExpenseSubmissionStatus;
import com.smartlab.erp.finance.enums.FinanceExpenseSubmissionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "finance_expense_submission", indexes = {
        @Index(name = "idx_finance_expense_submission_type", columnList = "submission_type"),
        @Index(name = "idx_finance_expense_submission_project", columnList = "project_id"),
        @Index(name = "idx_finance_expense_submission_submitter", columnList = "submitter_user_id"),
        @Index(name = "idx_finance_expense_submission_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceExpenseSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_type", nullable = false, length = 50)
    private FinanceExpenseSubmissionType submissionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FinanceExpenseSubmissionStatus status;

    @Column(name = "submitter_user_id", nullable = false, length = 64)
    private String submitterUserId;

    @Column(name = "submitter_name", nullable = false, length = 100)
    private String submitterName;

    @Column(name = "project_id", length = 64)
    private String projectId;

    @Column(name = "project_name", length = 150)
    private String projectName;

    @Column(name = "project_flow_type", length = 30)
    private String projectFlowType;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "item_category", length = 100)
    private String itemCategory;

    @Column(name = "item_specification", length = 200)
    private String itemSpecification;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "supplier_name", length = 150)
    private String supplierName;

    @Column(name = "invoice_number", nullable = false, length = 100)
    private String invoiceNumber;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "purpose", nullable = false, columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "departure_location", length = 120)
    private String departureLocation;

    @Column(name = "destination_location", length = 120)
    private String destinationLocation;

    @Column(name = "travel_start_at")
    private Instant travelStartAt;

    @Column(name = "travel_end_at")
    private Instant travelEndAt;

    @Column(name = "invoice_file_name", nullable = false, length = 255)
    private String invoiceFileName;

    @Column(name = "invoice_file_path", nullable = false, length = 500)
    private String invoiceFilePath;

    @Column(name = "invoice_content_type", length = 255)
    private String invoiceContentType;

    @Column(name = "invoice_file_size")
    private Long invoiceFileSize;

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
