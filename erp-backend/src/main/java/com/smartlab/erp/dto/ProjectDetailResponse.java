package com.smartlab.erp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartlab.erp.entity.ProjectType;
import com.smartlab.erp.enums.ProjectTierEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailResponse {

    private String id;
    private String name;
    private String description;
    private String managerId;
    private ProjectType type;
    private String status;
    private String projectStatus;
    private String productStatus;
    private String researchStatus;
    private String feasibilityReportUrl;
    private ProjectTierEnum projectTier;
    private String targetUsers;
    private String coreFeatures;
    private String useCase;
    private String problemStatement;
    private String techStackDesc;
    private String ideaOwnerUserId;
    private String promotionIcUserId;
    private String meetingParticipantUserIds;
    private String demoEngineeringOwnerUserId;
    private String demoFileOwnerUserId;
    private String demoDescriptionOwnerUserId;
    private String demoFeasibilityOwnerUserId;
    private String hostUserId;
    private String chiefEngineerUserId;
    private String blueprintOwnerUserId;
    private String architectureOwnerUserId;
    private String taskBreakdownOwnerUserId;
    private String evaluationReportOwnerUserId;

    // 🟢 核心修复：增加这个字段，后端才能把流转类型发给前端
    private String flowType;

    private BigDecimal budget;
    private BigDecimal cost;

    @JsonProperty("critical_task")
    private String criticalTask;

    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private List<MemberInfo> members = new ArrayList<>();

    @Builder.Default
    private List<MilestoneInfo> milestones = new ArrayList<>();

    @Builder.Default
    private List<UploadInfo> uploads = new ArrayList<>();

    @Builder.Default
    private List<ProjectSubtaskInfo> subtasks = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberInfo {
        private String userId;
        private String name;
        private String role;
        private String avatar;
        private Boolean hiddenAvatar;
        private Integer executionResponsibilityRatio;
        private Integer managerResponsibilityRatio;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MilestoneInfo {
        private String title;
        private String date;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadInfo {
        private Long id;
        private String name;
        private String type;
        private String user;
        private String time;
        private String url;
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectSubtaskInfo {
        private Long id;
        private String title;
        private String description;
        private String assigneeUserId;
        private String assigneeName;
        private Integer sortOrder;
        private boolean completed;
        private String completedAt;
    }
}
