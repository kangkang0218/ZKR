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
public class ReportGenerateRequest {

    @NotBlank(message = "请输入报表描述")
    private String text;

    private Boolean save;
}
