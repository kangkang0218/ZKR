package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceVentureRef {
    private String projectId;
    private Long legacyVentureId;
    private String displayName;
    private String legacyStage;
}
