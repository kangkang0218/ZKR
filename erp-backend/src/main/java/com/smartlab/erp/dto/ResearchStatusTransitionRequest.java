package com.smartlab.erp.dto;

import lombok.Data;

@Data
public class ResearchStatusTransitionRequest {
    private String toStatus;

    private Boolean blueprintExists;
    private Boolean smallGroupAllConfirmed;
    private Boolean taskPlanDefined;
    private Boolean researchTasksAssigned;
    private Double votePassRate;
    private Double votePassThreshold;

    private Boolean architectureDefined;
    private Boolean techRouteDefined;
    private Boolean taskBreakdownComplete;
    private String chiefEngineerUserId;

    private Boolean allModulesCompleted;
    private Boolean integrationSuccess;
    private Boolean currentVersionStable;
    private Boolean majorTasksCompleted;

    private Boolean evaluationCompleted;
    private String evaluationResult;

    private Boolean criticalIssue;
    private Boolean integrationFailed;
}
