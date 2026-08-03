package com.smartlab.erp.finance.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportSpec {

    private String title;
    private String chartType;
    private String source;
    private List<String> dimensions;
    private List<ReportMetric> metrics;
    private List<ReportFilter> filters;
}
