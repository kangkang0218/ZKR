package com.smartlab.erp.finance.dto;

import com.smartlab.erp.finance.enums.FinanceDividendStatus;
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
public class FinanceDividendItemView {
    private Long dividendSheetId;
    private FinanceVentureRef venture;
    private FinanceUserRef user;
    private String ledgerMonth;
    private BigDecimal amount;
    private BigDecimal dividendRatio;
    private BigDecimal netProfitSnapshot;
    private FinanceDividendStatus status;
    private Instant confirmedAt;
    private String confirmedBy;
}
