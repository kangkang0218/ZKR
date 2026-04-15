package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceCostPreviewItem {
    private String userId;
    private String userName;
    private String role;
    private BigDecimal workHours;
    private BigDecimal laborCost;
    private BigDecimal finalSettlementCost;
}
