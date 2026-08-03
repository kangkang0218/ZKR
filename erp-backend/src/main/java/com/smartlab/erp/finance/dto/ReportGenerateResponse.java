package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerateResponse {

    private ReportSpec spec;
    private List<ReportDataRow> rows;
    private int total;
    private Instant generatedAt;
    private String provider;
    private Long savedPromptId;
}
