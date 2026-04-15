package com.smartlab.erp.dto;

import com.smartlab.erp.enums.ProjectTierEnum;

public class ProjectDynamicInfoUpdateRequest {

    private String goalDescription;
    private ProjectTierEnum projectTier;
    private String techStackDescription;
    private String implementationStatus;

    public String getGoalDescription() {
        return goalDescription;
    }

    public void setGoalDescription(String goalDescription) {
        this.goalDescription = goalDescription;
    }

    public ProjectTierEnum getProjectTier() {
        return projectTier;
    }

    public void setProjectTier(ProjectTierEnum projectTier) {
        this.projectTier = projectTier;
    }

    public String getTechStackDescription() {
        return techStackDescription;
    }

    public void setTechStackDescription(String techStackDescription) {
        this.techStackDescription = techStackDescription;
    }

    public String getImplementationStatus() {
        return implementationStatus;
    }

    public void setImplementationStatus(String implementationStatus) {
        this.implementationStatus = implementationStatus;
    }
}
