package com.smartlab.erp.dto;

import com.smartlab.erp.enums.ProjectTierEnum;
import com.smartlab.erp.entity.ProjectType;
import java.math.BigDecimal;

/**
 * 项目发起请求 DTO
 */
public class ProjectInitiateRequestDTO {
    private String projectName;
    private BigDecimal estimatedRevenue;
    private String dataEngineerId; // User ID is String
    private String feasibilityReportUrl;
    private ProjectTierEnum projectTier;
    private ProjectType projectType;

    // Getters and Setters
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public BigDecimal getEstimatedRevenue() {
        return estimatedRevenue;
    }

    public void setEstimatedRevenue(BigDecimal estimatedRevenue) {
        this.estimatedRevenue = estimatedRevenue;
    }

    public String getDataEngineerId() {
        return dataEngineerId;
    }

    public void setDataEngineerId(String dataEngineerId) {
        this.dataEngineerId = dataEngineerId;
    }

    public String getFeasibilityReportUrl() {
        return feasibilityReportUrl;
    }

    public void setFeasibilityReportUrl(String feasibilityReportUrl) {
        this.feasibilityReportUrl = feasibilityReportUrl;
    }

    public ProjectTierEnum getProjectTier() {
        return projectTier;
    }

    public void setProjectTier(ProjectTierEnum projectTier) {
        this.projectTier = projectTier;
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }
}
