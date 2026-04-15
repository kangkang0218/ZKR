package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceResponseMeta {
    private Integer page;
    private Integer size;
    private Long total;
    private Integer totalPages;
    private String traceId;
    private Instant timestamp;

    public static FinanceResponseMeta empty() {
        return FinanceResponseMeta.builder().timestamp(Instant.now()).build();
    }
}
