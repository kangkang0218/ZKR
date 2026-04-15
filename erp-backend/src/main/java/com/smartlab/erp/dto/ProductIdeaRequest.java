package com.smartlab.erp.dto;

import com.smartlab.erp.entity.ProjectType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 阶段 1 - Idea 创意孵化接口请求体
 */
@Data
public class ProductIdeaRequest {

    /**
     * Idea 标题 / 拟定产品名
     */
    private String name;

    /**
     * 简要描述（可选）
     */
    private String description;

    /**
     * 预计项目投入（复用 SysProject.budget 字段）
     */
    private BigDecimal expectedBudget;

    /**
     * 产品行业分类
     */
    private ProjectType projectType;

    /**
     * 目标用户画像
     */
    private String targetUsers;

    /**
     * 核心功能 / 卖点
     */
    private String coreFeatures;

    /**
     * 主要用途
     */
    private String useCase;

    /**
     * 要解决的问题
     */
    private String problemStatement;

    /**
     * 技术栈与实现思路
     */
    private String techStackDesc;
}
