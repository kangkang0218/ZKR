package com.smartlab.erp.finance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class FinanceDividendConfirmResponse {
    private FinanceVentureRef venture;
    private String ledgerMonth;
    private int confirmedCount;
    private BigDecimal totalAmount;
    private BigDecimal bankBalanceBefore;
    private BigDecimal bankBalanceAfter;
    private List<FinanceMutationResult> walletResults;

    @JsonProperty("ventureId")
    public String getVentureId() {
        return venture == null ? null : venture.getProjectId();
    }

    @JsonProperty("results")
    public List<FinanceMutationResult> getResults() {
        return walletResults;
    }
}
