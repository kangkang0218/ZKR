package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceRagDataRow {
    private String title;
    private String snippet;
    private String sourceTable;
    private Long sourceId;
    private int score;
}
