package com.smartlab.erp.finance.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDividendPrepareRequest {

    @JsonAlias("ventureId")
    @NotBlank(message = "projectId is required")
    private String projectId;
}
