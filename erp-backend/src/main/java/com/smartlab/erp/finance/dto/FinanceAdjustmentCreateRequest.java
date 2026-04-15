package com.smartlab.erp.finance.dto;

import com.smartlab.erp.finance.enums.FinanceAdjustmentDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class FinanceAdjustmentCreateRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "subject is required")
    private String subject;

    @NotNull(message = "direction is required")
    private FinanceAdjustmentDirection direction;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    private String remark;

    private String reason;

    private Long sourceId;

    private String refDocNo;

    private String operator;
}
