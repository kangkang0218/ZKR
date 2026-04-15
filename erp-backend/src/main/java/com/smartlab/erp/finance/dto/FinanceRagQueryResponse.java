package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceRagQueryResponse {
    private String answer;
    private boolean readOnly;
    private List<String> approvedSourceTypes;
    private List<FinanceRagDataRow> dataRows;
    private List<FinanceAiContextBlock> contextBlocks;
}
