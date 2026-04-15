package com.smartlab.erp.finance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDividendConfirmRequest {

    @NotBlank(message = "projectId is required")
    private String projectId;

    private String operator;

    private String remark;
}
