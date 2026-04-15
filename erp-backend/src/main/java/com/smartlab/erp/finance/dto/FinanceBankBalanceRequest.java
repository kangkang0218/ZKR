package com.smartlab.erp.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceBankBalanceRequest {
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal balance;

    @NotBlank
    private String operator;

    private String remark;
}
