package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceExpenseSubmissionCenterResponse {
    private Summary summary;
    private List<Row> submissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long totalCount;
        private long procurementCount;
        private long travelCount;
        private BigDecimal totalAmount;
        private BigDecimal procurementAmount;
        private BigDecimal travelAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private Long id;
        private String submissionType;
        private String status;
        private String submitterUserId;
        private String submitterName;
        private String projectId;
        private String projectName;
        private String projectFlowType;
        private String itemName;
        private String itemCategory;
        private String itemSpecification;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalAmount;
        private String supplierName;
        private String invoiceNumber;
        private Instant occurredAt;
        private String purpose;
        private String remarks;
        private String departureLocation;
        private String destinationLocation;
        private Instant travelStartAt;
        private Instant travelEndAt;
        private String invoiceFileName;
        private Long invoiceFileSize;
        private Instant createdAt;
    }
}
