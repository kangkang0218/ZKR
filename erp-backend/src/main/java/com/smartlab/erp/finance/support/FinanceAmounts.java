package com.smartlab.erp.finance.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinanceAmounts {

    private FinanceAmounts() {
    }

    public static BigDecimal scale(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal add(BigDecimal left, BigDecimal right) {
        return scale(scale(left).add(scale(right)));
    }

    public static BigDecimal subtract(BigDecimal left, BigDecimal right) {
        return scale(scale(left).subtract(scale(right)));
    }
}
