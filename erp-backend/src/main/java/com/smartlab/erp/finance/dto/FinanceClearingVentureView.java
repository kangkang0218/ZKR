package com.smartlab.erp.finance.dto;

import com.smartlab.erp.finance.enums.FinanceClearingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceClearingVentureView {
    private FinanceVentureRef venture;
    private String ledgerMonth;
    private BigDecimal totalCost;
    private BigDecimal finalRevenue;
    private BigDecimal middlewareFee;
    private BigDecimal netProfit;
    private BigDecimal carryForwardLoss;
    private FinanceClearingStatus status;
    private Instant clearedAt;
    private Boolean costReady;
}
