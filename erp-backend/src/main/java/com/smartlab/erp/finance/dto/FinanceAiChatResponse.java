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
public class FinanceAiChatResponse {
    private String answer;
    private String provider;
    private String attemptedProvider;
    private String errorMessage;
    private boolean fallbackUsed;
    private String fallbackProvider;
    private String fallbackReason;
    private boolean readOnly;
    private boolean streaming;
    private List<String> approvedSourceTypes;
    private List<FinanceAiContextBlock> contextBlocks;
    private List<FinanceRagDataRow> dataRows;
}
