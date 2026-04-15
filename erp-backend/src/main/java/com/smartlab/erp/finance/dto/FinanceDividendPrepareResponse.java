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
public class FinanceDividendPrepareResponse {
    private FinanceVentureRef venture;
    private String ledgerMonth;
    private BigDecimal netProfit;
    private BigDecimal totalAmount;
    private List<FinanceDividendItemView> items;

    @JsonProperty("ventureId")
    public String getVentureId() {
        return venture == null ? null : venture.getProjectId();
    }

    @JsonProperty("rows")
    public List<FinanceDividendItemView> getRows() {
        return items;
    }
}
