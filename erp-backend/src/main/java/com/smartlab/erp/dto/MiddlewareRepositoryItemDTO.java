package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiddlewareRepositoryItemDTO {
    private Long middlewareId;
    private String middlewareName;
    private String createdAt;
    private String creator;
    private long invokeCount;
    private BigDecimal callPrice;
    private String currency;
}
