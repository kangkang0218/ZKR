package com.smartlab.erp.finance.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceLedgerMonthRequest {
    @NotBlank
    @JsonProperty("ledger_month")
    @JsonAlias("ledgerMonth")
    private String ledgerMonth;

    @JsonProperty("rerun_existing_month")
    @JsonAlias({"rerunExistingMonth", "replace_existing_month"})
    @Builder.Default
    private Boolean rerunExistingMonth = Boolean.FALSE;

    public boolean shouldRerunExistingMonth() {
        return Boolean.TRUE.equals(rerunExistingMonth);
    }
}
