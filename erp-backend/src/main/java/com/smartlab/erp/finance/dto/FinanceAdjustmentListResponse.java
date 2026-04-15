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
public class FinanceAdjustmentListResponse {
    private List<FinanceAdjustmentItemView> items;
    private long totalCount;
    private BigDecimal debitTotal;
    private BigDecimal creditTotal;
    private BigDecimal netAdjustment;

    @JsonProperty("rows")
    public List<FinanceAdjustmentItemView> getRows() {
        return items;
    }
}
