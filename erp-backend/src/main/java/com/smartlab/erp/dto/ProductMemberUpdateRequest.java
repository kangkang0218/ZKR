package com.smartlab.erp.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductMemberUpdateRequest {
    private List<String> addUserIds;
    private List<String> removeUserIds;
    private Integer managerWeight;
    private Integer managerExecutionWeight;
    private List<ResponsibilityMemberItem> responsibilityMembers;

    @Data
    public static class ResponsibilityMemberItem {
        private String userId;
        private String role;
        private Integer weight;
    }
}
