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
public class FinanceDividendListResponse {
    private List<FinanceDividendItemView> items;
    private long totalCount;
    private BigDecimal totalAmount;

    @JsonProperty("rows")
    public List<FinanceDividendItemView> getRows() {
        return items;
    }
}
