package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceRagPushResponse {
    private String indexName;
    private String status;
    private int documentCount;
    private String message;
}
