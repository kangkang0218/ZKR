package com.smartlab.erp.finance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlab.erp.finance.dto.ReportSpec;
import com.smartlab.erp.finance.enums.ReportSource;
import com.smartlab.erp.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSpecParserService {

    private static final String SYSTEM_PROMPT = """
            你是企业财务一句话报表助手。用户会用一句话描述想要的报表，你必须把它转换为严格的 JSON 报表规格（ReportSpec），用于驱动数据库查询和图表渲染。

            今天是 %s。

            可选数据源（source 必须取其一）：
            %s

            chartType 取值：bar 柱状图 / line 折线图 / pie 饼图 / table 明细表格 / number 数字卡片
            - 饼图 pie 和数字卡片 number 只能 1 个维度、1 个度量
            - 时间趋势用 line，分类对比用 bar，占比分布用 pie，明细列表用 table

            metrics 的 agg 取值：sum / avg / count / min / max（余额 balance 建议用 avg）
            filters 的 op 取值：eq / in / contains / gte / lte

            规则：
            1. 用户说"近 N 个月/近 N 天/本月/上个月/最近一周"等相对时间时，必须结合今天日期换算成具体的 month 或 date 过滤值（in / gte / lte），不要使用相对词。
            2. 没有明确时间或条件就省略 filters。
            3. 维度、度量字段必须严格使用数据源中列出的字段名，不得编造。
            4. 只输出纯 JSON（不要 Markdown 代码块、不要解释），格式：
            {"title":"报表标题","chartType":"bar","source":"expense_submission","dimensions":["projectName"],"metrics":[{"field":"totalAmount","agg":"sum"}],"filters":[]}
            """;

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public ReportSpec parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("请输入报表描述");
        }
        String systemPrompt = SYSTEM_PROMPT.formatted(LocalDate.now(), ReportSource.supportedSourcesText());
        String content = llmClient.chatCompletion(systemPrompt, text);
        content = stripThinkTags(content);

        try {
            return objectMapper.readValue(content, ReportSpec.class);
        } catch (Exception e) {
            log.error("Failed to parse LLM response as ReportSpec: {}", content, e);
            throw new RuntimeException("无法解析大模型返回的报表规格，请换一种描述方式重试", e);
        }
    }

    private String stripThinkTags(String content) {
        if (content == null) {
            return "";
        }
        String stripped = content.replaceAll("(?s)<think>.*?</think>", "").trim();
        if (stripped.isEmpty()) {
            return content.trim();
        }
        return stripped;
    }
}
