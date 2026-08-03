package com.smartlab.erp.finance.service;

import com.smartlab.erp.finance.dto.ReportDataRow;
import com.smartlab.erp.finance.dto.ReportFilter;
import com.smartlab.erp.finance.dto.ReportMetric;
import com.smartlab.erp.finance.dto.ReportSpec;
import com.smartlab.erp.finance.entity.FinanceBankBalanceSnapshot;
import com.smartlab.erp.finance.entity.FinanceCostEntry;
import com.smartlab.erp.finance.entity.FinanceExpenseSubmission;
import com.smartlab.erp.finance.enums.ReportSource;
import com.smartlab.erp.finance.repository.FinanceBankBalanceSnapshotRepository;
import com.smartlab.erp.finance.repository.FinanceCostEntryRepository;
import com.smartlab.erp.finance.repository.FinanceExpenseSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportDataService {

    private static final Set<String> CHART_TYPES = Set.of("bar", "line", "pie", "table", "number");
    private static final Set<String> AGG_FUNCTIONS = Set.of("sum", "avg", "count", "min", "max");
    private static final Set<String> FILTER_OPS = Set.of("eq", "in", "contains", "gte", "lte");
    private static final int MAX_ROWS = 500;
    private static final int MAX_DIMENSIONS = 2;
    private static final int MAX_METRICS = 2;

    private final FinanceExpenseSubmissionRepository expenseSubmissionRepository;
    private final FinanceCostEntryRepository costEntryRepository;
    private final FinanceBankBalanceSnapshotRepository bankBalanceSnapshotRepository;

    @Transactional(readOnly = true)
    public List<ReportDataRow> aggregate(ReportSpec spec) {
        validate(spec);
        ReportSource source = Objects.requireNonNull(ReportSource.fromKey(spec.getSource()));
        List<Object> rows = loadRows(source);
        List<Map<String, Object>> extracted = rows.stream().map(source::extract).collect(Collectors.toList());

        List<Map<String, Object>> filtered = applyFilters(extracted, spec.getFilters());
        return groupAndAggregate(filtered, spec.getDimensions(), spec.getMetrics());
    }

    public void validate(ReportSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("报表规格不能为空");
        }
        if (spec.getChartType() == null || !CHART_TYPES.contains(spec.getChartType())) {
            throw new IllegalArgumentException("不支持的图表类型：" + spec.getChartType()
                    + "，支持 bar/line/pie/table/number");
        }
        ReportSource source = ReportSource.fromKey(spec.getSource());
        if (source == null) {
            throw new IllegalArgumentException("不支持的数据源，请使用以下之一：\n" + ReportSource.supportedSourcesText());
        }
        List<String> dimensions = spec.getDimensions() == null ? List.of() : spec.getDimensions();
        if (dimensions.size() > MAX_DIMENSIONS) {
            throw new IllegalArgumentException("维度最多 " + MAX_DIMENSIONS + " 个");
        }
        for (String dimension : dimensions) {
            if (!source.getDimensionFields().contains(dimension)) {
                throw new IllegalArgumentException("数据源 " + source.getSourceKey() + "（" + source.getLabel()
                        + "）不支持维度字段 " + dimension + "，可用维度：" + String.join("/", source.getDimensionFields()));
            }
        }
        List<ReportMetric> metrics = spec.getMetrics();
        if (metrics == null || metrics.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个度量字段，例如 " + source.getDefaultMetricField());
        }
        if (metrics.size() > MAX_METRICS) {
            throw new IllegalArgumentException("度量最多 " + MAX_METRICS + " 个");
        }
        for (ReportMetric metric : metrics) {
            if (metric == null || metric.getField() == null || !source.getMetricFields().contains(metric.getField())) {
                throw new IllegalArgumentException("数据源 " + source.getSourceKey() + "（" + source.getLabel()
                        + "）不支持度量字段，可用度量：" + String.join("/", source.getMetricFields()));
            }
            if (metric.getAgg() == null || !AGG_FUNCTIONS.contains(metric.getAgg())) {
                throw new IllegalArgumentException("不支持的聚合方式：" + metric.getAgg()
                        + "，支持 sum/avg/count/min/max");
            }
        }
        if ("pie".equals(spec.getChartType()) || "number".equals(spec.getChartType())) {
            if (dimensions.size() > 1 || metrics.size() > 1) {
                throw new IllegalArgumentException("饼图和数字卡片只支持 1 个维度、1 个度量");
            }
        }
        Set<String> allowedFilterFields = new HashSet<>(source.getDimensionFields());
        allowedFilterFields.addAll(source.getMetricFields());
        if (spec.getFilters() != null) {
            for (ReportFilter filter : spec.getFilters()) {
                if (filter.getField() == null || !allowedFilterFields.contains(filter.getField())) {
                    throw new IllegalArgumentException("筛选字段 " + filter.getField()
                            + " 不在支持范围内，可用字段：" + String.join("/", allowedFilterFields));
                }
                if (filter.getOp() == null || !FILTER_OPS.contains(filter.getOp())) {
                    throw new IllegalArgumentException("不支持的筛选方式：" + filter.getOp()
                            + "，支持 eq/in/contains/gte/lte");
                }
                if (filter.getValues() == null || filter.getValues().isEmpty()) {
                    throw new IllegalArgumentException("筛选 " + filter.getField() + " 缺少过滤值");
                }
            }
        }
    }

    private List<Object> loadRows(ReportSource source) {
        return switch (source) {
            case EXPENSE_SUBMISSION -> new ArrayList<>(expenseSubmissionRepository.findAll());
            case COST_ENTRY -> new ArrayList<>(costEntryRepository.findAll());
            case BANK_BALANCE -> new ArrayList<>(bankBalanceSnapshotRepository.findAll());
        };
    }

    private List<Map<String, Object>> applyFilters(List<Map<String, Object>> rows, List<ReportFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return rows;
        }
        return rows.stream().filter(row -> filters.stream().allMatch(filter -> matches(row, filter)))
                .collect(Collectors.toList());
    }

    private boolean matches(Map<String, Object> row, ReportFilter filter) {
        Object raw = row.get(filter.getField());
        String op = filter.getOp();
        List<String> values = filter.getValues();
        boolean isNumeric = raw instanceof Number;
        if ("eq".equals(op)) {
            return values.stream().anyMatch(value -> equalsValue(raw, value));
        }
        if ("in".equals(op)) {
            return values.stream().anyMatch(value -> equalsValue(raw, value));
        }
        if ("contains".equals(op)) {
            String text = raw == null ? "" : String.valueOf(raw);
            return values.stream().anyMatch(value -> text.contains(value));
        }
        if ("gte".equals(op)) {
            return values.stream().anyMatch(value -> compareValues(raw, value) >= 0);
        }
        if ("lte".equals(op)) {
            return values.stream().anyMatch(value -> compareValues(raw, value) <= 0);
        }
        return false;
    }

    private boolean equalsValue(Object raw, String value) {
        if (raw == null) {
            return value == null;
        }
        return String.valueOf(raw).trim().equalsIgnoreCase(value.trim());
    }

    private int compareValues(Object raw, String value) {
        if (raw instanceof Number number) {
            return new BigDecimal(number.toString()).compareTo(toDecimal(value));
        }
        return String.valueOf(raw).compareTo(value);
    }

    private BigDecimal toDecimal(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private List<ReportDataRow> groupAndAggregate(List<Map<String, Object>> rows,
                                                  List<String> dimensions,
                                                  List<ReportMetric> metrics) {
        Map<String, GroupAccumulator> groups = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Map<String, String> dimValues = new LinkedHashMap<>();
            for (String dimension : dimensions) {
                Object raw = row.get(dimension);
                dimValues.put(dimension, raw == null ? "未知" : String.valueOf(raw));
            }
            String key = dimValues.isEmpty() ? "全部" : String.join(" | ", dimValues.values());
            GroupAccumulator accumulator = groups.computeIfAbsent(key, ignored -> new GroupAccumulator(key, dimValues));
            accumulator.add(row, metrics);
        }

        List<ReportDataRow> result = new ArrayList<>();
        groups.forEach((key, accumulator) -> result.add(accumulator.toRow(metrics)));

        if (!result.isEmpty()) {
            String firstMetric = metrics.get(0).getField();
            result.sort((left, right) -> {
                BigDecimal leftValue = left.getValues().getOrDefault(firstMetric, BigDecimal.ZERO);
                BigDecimal rightValue = right.getValues().getOrDefault(firstMetric, BigDecimal.ZERO);
                return rightValue.compareTo(leftValue);
            });
        }
        if (result.size() > MAX_ROWS) {
            return result.subList(0, MAX_ROWS);
        }
        return result;
    }

    private static class GroupAccumulator {
        private final String key;
        private final Map<String, String> dimValues;
        private final Map<String, BigDecimal> sums = new HashMap<>();
        private final Map<String, BigDecimal> mins = new HashMap<>();
        private final Map<String, BigDecimal> maxs = new HashMap<>();
        private long count;

        GroupAccumulator(String key, Map<String, String> dimValues) {
            this.key = key;
            this.dimValues = dimValues;
        }

        void add(Map<String, Object> row, List<ReportMetric> metrics) {
            count++;
            for (ReportMetric metric : metrics) {
                Object raw = row.get(metric.getField());
                BigDecimal value = raw instanceof Number ? new BigDecimal(raw.toString()) : null;
                if (value != null) {
                    sums.merge(metric.getField(), value, BigDecimal::add);
                    mins.merge(metric.getField(), value, BigDecimal::min);
                    maxs.merge(metric.getField(), value, BigDecimal::max);
                }
            }
        }

        ReportDataRow toRow(List<ReportMetric> metrics) {
            Map<String, BigDecimal> values = new LinkedHashMap<>();
            for (ReportMetric metric : metrics) {
                BigDecimal sum = sums.get(metric.getField());
                BigDecimal min = mins.get(metric.getField());
                BigDecimal max = maxs.get(metric.getField());
                switch (metric.getAgg()) {
                    case "count" -> values.put(metric.getField(), BigDecimal.valueOf(count));
                    case "avg" -> values.put(metric.getField(), sum == null
                            ? BigDecimal.ZERO.setScale(2)
                            : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
                    case "min" -> values.put(metric.getField(), min == null ? BigDecimal.ZERO.setScale(2) : min);
                    case "max" -> values.put(metric.getField(), max == null ? BigDecimal.ZERO.setScale(2) : max);
                    default -> values.put(metric.getField(), sum == null ? BigDecimal.ZERO.setScale(2) : sum);
                }
            }
            return ReportDataRow.builder()
                    .key(key)
                    .dimensions(dimValues)
                    .values(values)
                    .build();
        }
    }
}
