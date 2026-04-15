package com.smartlab.erp.finance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private FinanceResponseMeta meta;
    private Instant timestamp;
    private String traceId;

    public static <T> FinanceApiResponse<T> success(String message, T data) {
        return success(message, data, null, null);
    }

    public static <T> FinanceApiResponse<T> success(String message, T data, FinanceResponseMeta meta, String traceId) {
        Instant now = Instant.now();
        FinanceResponseMeta resolvedMeta = FinanceResponseMeta.builder()
                .page(meta == null ? null : meta.getPage())
                .size(meta == null ? null : meta.getSize())
                .total(meta == null ? null : meta.getTotal())
                .totalPages(meta == null ? null : meta.getTotalPages())
                .traceId(traceId)
                .timestamp(now)
                .build();
        return FinanceApiResponse.<T>builder()
                .status("success")
                .message(message)
                .data(data)
                .meta(resolvedMeta)
                .timestamp(now)
                .traceId(traceId)
                .build();
    }

    public static <T> FinanceApiResponse<T> failure(String message, String traceId) {
        Instant now = Instant.now();
        return FinanceApiResponse.<T>builder()
                .status("error")
                .message(message)
                .data(null)
                .timestamp(now)
                .traceId(traceId)
                .meta(FinanceResponseMeta.builder().traceId(traceId).timestamp(now).build())
                .build();
    }

    @JsonProperty("success")
    public boolean isSuccess() {
        return "success".equals(status);
    }
}
