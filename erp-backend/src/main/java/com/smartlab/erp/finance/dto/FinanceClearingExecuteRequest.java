package com.smartlab.erp.finance.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class FinanceClearingExecuteRequest {
    @NotNull
    @JsonProperty("venture_id")
    private Long ventureId;

    @NotNull
    @DecimalMin(value = "0.00")
    @JsonProperty("final_revenue")
    private BigDecimal finalRevenue;

    @JsonIgnore
    @Builder.Default
    private Map<String, Object> unsupportedFields = new LinkedHashMap<>();

    @JsonAnySetter
    public void captureUnsupportedField(String fieldName, Object value) {
        unsupportedFields.put(fieldName, value);
    }

    @JsonIgnore
    public boolean hasUnsupportedFields() {
        return !unsupportedFields.isEmpty();
    }
}
