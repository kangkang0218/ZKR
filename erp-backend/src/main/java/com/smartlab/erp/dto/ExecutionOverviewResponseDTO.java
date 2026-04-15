package com.smartlab.erp.dto;

import com.smartlab.erp.enums.FolderTypeEnum;
import com.smartlab.erp.enums.ProjectTierEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionOverviewResponseDTO {

    private boolean canManage;
    private boolean canUploadManagerFiles;
    private boolean canUploadEngineerFiles;

    private PlanInfo plan;

    @Builder.Default
    private List<MemberScheduleInfo> schedules = new ArrayList<>();

    @Builder.Default
    private List<ExecutionFileInfo> managerArchiveFiles = new ArrayList<>();

    @Builder.Default
    private List<ArchiveFolderInfo> managerArchiveFolders = new ArrayList<>();

    @Builder.Default
    private List<ExecutionFileInfo> engineerWorkspaceFiles = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArchiveFolderInfo {
        private Long id;
        private String folderPath;
        private String folderName;
        private String parentPath;
        private boolean canManage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanInfo {
        private String difficultyLevel;
        private ProjectTierEnum projectTier;
        private String goalDescription;
        private String techStackDescription;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberScheduleInfo {
        private String userId;
        private String name;
        private String role;
        private String taskName;
        private String expectedOutput;
        private String expectedStartDate;
        private String expectedEndDate;
        private String actualEndDate;
        private boolean completed;
        private boolean managerConfirmed;
        private String managerConfirmedAt;
        private long delayDays;
        private Integer executionResponsibilityRatio;
        private Integer managerResponsibilityRatio;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionFileInfo {
        private Long id;
        private String fileName;
        private String fileType;
        private String uploaderUserId;
        private String uploaderName;
        private String uploadedAt;
        private Long fileSize;
        private FolderTypeEnum folderType;
        private String secondaryCategory;
        private boolean canDownload;
        private boolean canDelete;
        private boolean canRecategorize;
    }
}
