package com.smartlab.erp.dto;

import com.smartlab.erp.enums.BusinessRoleEnum;

/**
 * 团队成员 DTO
 */
public class MemberDTO {
    private String userId;
    private BusinessRoleEnum role;
    private Integer weight;

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BusinessRoleEnum getRole() {
        return role;
    }

    public void setRole(BusinessRoleEnum role) {
        this.role = role;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
