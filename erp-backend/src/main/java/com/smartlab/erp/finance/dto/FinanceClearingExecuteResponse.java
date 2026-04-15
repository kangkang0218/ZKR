package com.smartlab.erp.finance.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartlab.erp.finance.enums.FinanceClearingStatus;
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
public class FinanceClearingExecuteResponse {
    @JsonProperty("clearing_sheet_id")
    private Long clearingSheetId;
    private FinanceVentureRef venture;

    @JsonProperty("ledger_month")
    private String ledgerMonth;

    @JsonProperty("final_revenue")
    private BigDecimal finalRevenue;

    @JsonProperty("total_cost")
    private BigDecimal totalCost;

    @JsonProperty("middleware_fee")
    private BigDecimal middlewareFee;

    @JsonProperty("net_profit")
    private BigDecimal netProfit;

    @JsonProperty("loss_transferred_to_company")
    private BigDecimal lossTransferredToCompany;

    private FinanceClearingStatus status;

    @JsonProperty("cleared_at")
    private Instant clearedAt;

    @JsonProperty("royalty_items")
    private List<FinanceClearingRoyaltyItem> royaltyItems;

    @JsonIgnore
    public BigDecimal getCarryForwardLoss() {
        return lossTransferredToCompany;
    }

    @JsonIgnore
    public void setCarryForwardLoss(BigDecimal carryForwardLoss) {
        this.lossTransferredToCompany = carryForwardLoss;
    }
}
