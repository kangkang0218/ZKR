package com.smartlab.erp.enums;

/**
 * 业务角色枚举
 */
public enum BusinessRoleEnum {
    BUSINESS("商务"),
    DATA("数据工程师"),
    DEV("开发"),
    ALGORITHM("算法"),
    PM("项目经理"),
    MANAGER("项目管理员"); // 动态权限标签

    private final String description;

    BusinessRoleEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
