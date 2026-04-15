package com.smartlab.erp.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 科研流发起请求体
 */
@Data
public class ResearchInitiateRequest {

    /**
     * 创新点说明
     */
    private String innovationPoint;

    /**
     * Idea 主体内容
     */
    private String idea;

    /**
     * 预计投入预算
     */
    private BigDecimal budget;

    /**
     * 可选：指定主持人，不填则发起人兼任
     */
    private String hostUserId;

    /**
     * 可选：提前指定总工程师
     */
    private String chiefEngineerUserId;

    /**
     * 核心成员用户 ID 列表（2~3 人）
     */
    private List<String> coreMemberIds;
}
