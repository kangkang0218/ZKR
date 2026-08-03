package com.smartlab.erp.finance.enums;

import com.smartlab.erp.finance.entity.FinanceBankBalanceSnapshot;
import com.smartlab.erp.finance.entity.FinanceCostEntry;
import com.smartlab.erp.finance.entity.FinanceExpenseSubmission;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 一句话报表支持的数据源白名单。每个数据源声明可用的维度字段、度量字段，
 * 并提供统一的行级字段抽取，由 ReportDataService 做内存过滤与聚合。
 */
public enum ReportSource {

    EXPENSE_SUBMISSION("expense_submission", "费用报销", Set.of(
            "projectName", "itemCategory", "submissionType", "status",
            "submitterName", "supplierName", "month", "date"), Set.of(
            "totalAmount", "quantity", "unitPrice"), "totalAmount") {
        @Override
        public Map<String, Object> extract(Object row) {
            FinanceExpenseSubmission r = (FinanceExpenseSubmission) row;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("projectName", orUnknown(r.getProjectName()));
            values.put("itemCategory", orUnknown(r.getItemCategory()));
            values.put("submissionType", r.getSubmissionType() == null ? "" : r.getSubmissionType().name());
            values.put("status", r.getStatus() == null ? "" : r.getStatus().name());
            values.put("submitterName", orUnknown(r.getSubmitterName()));
            values.put("supplierName", orUnknown(r.getSupplierName()));
            values.put("month", formatMonth(r.getOccurredAt()));
            values.put("date", formatDate(r.getOccurredAt()));
            values.put("totalAmount", r.getTotalAmount());
            values.put("quantity", r.getQuantity());
            values.put("unitPrice", r.getUnitPrice());
            return values;
        }
    },

    COST_ENTRY("cost_entry", "人力成本台账", Set.of(
            "projectName", "ledgerMonth", "userName", "role"), Set.of(
            "workHours", "laborCost", "middlewareRoyaltyFee", "finalSettlementCost"), "laborCost") {
        @Override
        public Map<String, Object> extract(Object row) {
            FinanceCostEntry r = (FinanceCostEntry) row;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("projectName", r.getProject() == null ? "未知项目" : orUnknown(r.getProject().getName()));
            values.put("ledgerMonth", orUnknown(r.getLedgerMonth()));
            values.put("userName", r.getUser() == null ? "未知成员" : orUnknown(r.getUser().getName()));
            values.put("role", r.getUser() == null || r.getUser().getRole() == null ? "" : r.getUser().getRole());
            values.put("workHours", r.getWorkHours());
            values.put("laborCost", r.getLaborCost());
            values.put("middlewareRoyaltyFee", r.getMiddlewareRoyaltyFee());
            values.put("finalSettlementCost", r.getFinalSettlementCost());
            return values;
        }
    },

    BANK_BALANCE("bank_balance", "银行余额快照", Set.of(
            "month", "operator"), Set.of(
            "balance"), "balance") {
        @Override
        public Map<String, Object> extract(Object row) {
            FinanceBankBalanceSnapshot r = (FinanceBankBalanceSnapshot) row;
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("month", formatMonth(r.getSnapshotAt()));
            values.put("operator", orUnknown(r.getOperator()));
            values.put("balance", r.getBalance());
            return values;
        }
    };

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String sourceKey;
    private final String label;
    private final Set<String> dimensionFields;
    private final Set<String> metricFields;
    private final String defaultMetricField;

    ReportSource(String sourceKey, String label, Set<String> dimensionFields,
                 Set<String> metricFields, String defaultMetricField) {
        this.sourceKey = sourceKey;
        this.label = label;
        this.dimensionFields = dimensionFields;
        this.metricFields = metricFields;
        this.defaultMetricField = defaultMetricField;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public String getLabel() {
        return label;
    }

    public Set<String> getDimensionFields() {
        return dimensionFields;
    }

    public Set<String> getMetricFields() {
        return metricFields;
    }

    public String getDefaultMetricField() {
        return defaultMetricField;
    }

    public abstract Map<String, Object> extract(Object row);

    public static ReportSource fromKey(String key) {
        for (ReportSource source : values()) {
            if (source.sourceKey.equals(key)) {
                return source;
            }
        }
        return null;
    }

    public static String supportedSourcesText() {
        StringBuilder builder = new StringBuilder();
        for (ReportSource source : values()) {
            builder.append("- ").append(source.sourceKey).append("（").append(source.label)
                    .append("）：维度字段 ").append(String.join("/", source.dimensionFields))
                    .append("；度量字段 ").append(String.join("/", source.metricFields)).append("\n");
        }
        return builder.toString();
    }

    private static String orUnknown(String value) {
        return value == null || value.isBlank() ? "未知" : value;
    }

    private static String formatMonth(Instant instant) {
        if (instant == null) {
            return "未知";
        }
        return MONTH_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }

    private static String formatDate(Instant instant) {
        if (instant == null) {
            return "未知";
        }
        return DATE_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }
}
