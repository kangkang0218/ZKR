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
public class FinanceRagQueryRequest {
    @NotBlank
    private String prompt;
    private Integer limit;
}
