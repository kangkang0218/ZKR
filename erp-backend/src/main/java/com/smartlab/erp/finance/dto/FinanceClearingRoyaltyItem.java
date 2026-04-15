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
public class FinanceClearingRoyaltyItem {
    private Long middlewareId;
    private String middlewareName;
    private String userId;
    private String userName;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
}
