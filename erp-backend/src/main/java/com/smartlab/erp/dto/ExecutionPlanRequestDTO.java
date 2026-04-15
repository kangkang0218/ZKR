package com.smartlab.erp.dto;

import com.smartlab.erp.enums.ProjectTierEnum;

import java.util.List;

/**
 * 实施阶段计划设定请求 DTO
 * 
 * 由 Manager 调用，用于设定项目实施计划。
 * 包含难度分级、技术栈描述和每个成员的排期信息。
 */
public class ExecutionPlanRequestDTO {

    /** 预评估项目难度分级 (如: 简单/中等/困难/极限) */
    private String difficultyLevel;

    /** 强制目标描述 */
    private String goalDescription;

    /** 可能涉及的技术栈和深度描述 */
    private String techStackDescription;

    private ProjectTierEnum projectTier;

    /** 成员排期列表 */
    private List<ScheduleDTO> memberSchedules;

    // ===== Getters & Setters =====

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getGoalDescription() {
        return goalDescription;
    }

    public void setGoalDescription(String goalDescription) {
        this.goalDescription = goalDescription;
    }

    public String getTechStackDescription() {
        return techStackDescription;
    }

    public void setTechStackDescription(String techStackDescription) {
        this.techStackDescription = techStackDescription;
    }

    public ProjectTierEnum getProjectTier() {
        return projectTier;
    }

    public void setProjectTier(ProjectTierEnum projectTier) {
        this.projectTier = projectTier;
    }

    public List<ScheduleDTO> getMemberSchedules() {
        return memberSchedules;
    }

    public void setMemberSchedules(List<ScheduleDTO> memberSchedules) {
        this.memberSchedules = memberSchedules;
    }
}
