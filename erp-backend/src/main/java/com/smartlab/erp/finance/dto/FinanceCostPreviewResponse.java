package com.smartlab.erp.finance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartlab.erp.finance.enums.FinanceBatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceCostPreviewResponse {
    private FinanceVentureRef venture;

    @JsonProperty("ledger_month")
    private String ledgerMonth;

    @JsonProperty("batch_id")
    private Long batchId;

    @JsonProperty("batch_status")
    private FinanceBatchStatus batchStatus;

    @JsonProperty("entry_count")
    private Integer entryCount;

    @JsonProperty("total_settlement_cost")
    private BigDecimal totalSettlementCost;

    private List<FinanceCostPreviewItem> items;
}
