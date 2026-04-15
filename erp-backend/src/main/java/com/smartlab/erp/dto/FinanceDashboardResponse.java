package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDashboardResponse {

    private List<FlowProjects> projects;
    private List<FlowProjects> products;
    private List<FlowProjects> research;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlowProjects {
        private String projectId;
        private String name;
        private String projectType;
        private String projectTier;
        private String status;
        private String managerName;
        private String managerId;
        private String primaryOwnerName;
        private String primaryOwnerId;
        private BigDecimal budget;
        private BigDecimal cost;
        private List<MemberInfo> members;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberInfo {
        private String userId;
        private String name;
        private String role;
    }
}
