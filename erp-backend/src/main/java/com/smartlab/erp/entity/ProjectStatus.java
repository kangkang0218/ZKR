package com.smartlab.erp.entity;

/**
 * 项目流状态枚举（轨道 A）
 * 生命周期：线索 -> 投标 -> 立项 -> 实施 -> 验收
 */
public enum ProjectStatus {
    LEAD("线索", "项目线索和初步接触"),
    BIDDING("投标", "投标准备和提交"),
    INITIATED("发起阶段", "项目发起和立项审批"), // 对应 INITIATION
    TEAM_FORMATION("组队阶段", "项目团队组建"), // 新增
    IMPLEMENTING("实施", "项目实施阶段"), // 对应 EXECUTION
    ACCEPTANCE("验收", "项目验收阶段"),
    SETTLEMENT("结算归档", "项目结算和归档"), // 新增
    COMPLETED("已完成", "项目验收完成");

    private final String stageName;
    private final String description;

    ProjectStatus(String stageName, String description) {
        this.stageName = stageName;
        this.description = description;
    }

    public String getStageName() {
        return stageName;
    }

    public String getDescription() {
        return description;
    }
}
