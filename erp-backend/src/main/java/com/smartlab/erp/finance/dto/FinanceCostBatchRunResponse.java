package com.smartlab.erp.finance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartlab.erp.finance.enums.FinanceBatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceCostBatchRunResponse {
    @JsonProperty("batch_id")
    private Long batchId;

    @JsonProperty("ledger_month")
    private String ledgerMonth;

    private FinanceBatchStatus status;

    @JsonProperty("venture_count")
    private Integer ventureCount;

    @JsonProperty("generated_record_count")
    private Integer generatedRecordCount;

    @JsonProperty("total_settlement_cost")
    private BigDecimal totalSettlementCost;

    @JsonProperty("reused_existing_batch")
    private Boolean reusedExistingBatch;
}
