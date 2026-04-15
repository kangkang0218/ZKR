package com.smartlab.erp.enums;

/**
 * 项目评级枚举
 */
public enum ProjectTierEnum {
    S("S级"),
    A("A级"),
    B("B级"),
    C("C级"),
    N("N级");

    private final String description;

    ProjectTierEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
