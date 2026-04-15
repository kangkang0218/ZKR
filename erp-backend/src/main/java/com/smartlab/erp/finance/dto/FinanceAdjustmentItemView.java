package com.smartlab.erp.finance.dto;

import com.smartlab.erp.finance.enums.FinanceAdjustmentDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceAdjustmentItemView {
    private Long adjustmentId;
    private FinanceUserRef user;
    private String subject;
    private FinanceAdjustmentDirection direction;
    private BigDecimal amount;
    private String remark;
    private String refDocNo;
    private FinanceAuditRef audit;
    private String operator;
    private Instant createdAt;
}
