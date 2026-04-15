package com.smartlab.erp.enums;

/**
 * 项目流状态枚举 (Project Flow Stage)
 * 
 * 定义项目流的四个阶段，与 ProjectStatus 中的状态值形成映射关系：
 * - INITIATION  → ProjectStatus.INITIATED
 * - TEAM_FORMATION → ProjectStatus.TEAM_FORMATION
 * - EXECUTION   → ProjectStatus.IMPLEMENTING
 * - SETTLEMENT  → ProjectStatus.SETTLEMENT
 *
 * 此枚举用于领域语义清晰化，不替代 ProjectStatus，仅作为业务层的语义辅助。
 */
public enum ProjectStageEnum {

    INITIATION("发起阶段", "项目由商务(BD)发起，录入基本信息与可行性报告"),
    TEAM_FORMATION("组队阶段", "数据工程师组建项目团队，指定 Manager"),
    EXECUTION("实施阶段", "Manager 制定计划，团队执行交付"),
    SETTLEMENT("结算归档", "OCR 凭证校验通过后，归档并结算");

    private final String stageName;
    private final String description;

    ProjectStageEnum(String stageName, String description) {
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
