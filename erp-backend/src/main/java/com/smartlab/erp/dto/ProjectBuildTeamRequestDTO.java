package com.smartlab.erp.dto;

import java.util.List;

/**
 * 组建团队请求 DTO
 */
public class ProjectBuildTeamRequestDTO {
    private List<MemberDTO> teamMembers;
    private String managerUserId;
    private Integer managerWeight;
    private Integer managerExecutionWeight;

    // Getters and Setters
    public List<MemberDTO> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<MemberDTO> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public String getManagerUserId() {
        return managerUserId;
    }

    public void setManagerUserId(String managerUserId) {
        this.managerUserId = managerUserId;
    }

    public Integer getManagerWeight() {
        return managerWeight;
    }

    public void setManagerWeight(Integer managerWeight) {
        this.managerWeight = managerWeight;
    }

    public Integer getManagerExecutionWeight() {
        return managerExecutionWeight;
    }

    public void setManagerExecutionWeight(Integer managerExecutionWeight) {
        this.managerExecutionWeight = managerExecutionWeight;
    }
}
