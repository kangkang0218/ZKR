package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceTransactionListResponse {
    private Integer limit;
    private Long totalCount;
    private List<TransactionRow> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionRow {
        private Long id;
        private FinanceUserRef owner;
        private FinanceVentureRef venture;
        private String transactionType;
        private String cashFlowDirection;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private String sourceTable;
        private String sourceBusinessId;
        private FinanceAuditRef audit;
        private String remark;
        private Instant createdAt;
    }
}
