package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceAuditRef {
    private String sourceTable;
    private Long sourceId;
}
