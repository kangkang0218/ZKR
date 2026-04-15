package com.smartlab.erp.enums;

/**
 * 实施阶段文件隔离区类型枚举
 * 
 * 用于实现"双盲文件隔离"机制：
 * - A_MANAGER_ARCHIVE: Manager 专属归档区，仅 Manager 可上传和查看
 * - B_ENGINEER_WORK:   工程师作业区，仅非 Manager 成员可上传，工程师之间互不可见，Manager 可统筹查看
 */
public enum FolderTypeEnum {

    A_MANAGER_ARCHIVE("Manager归档区", "仅项目 Manager 可上传/查看"),
    B_ENGINEER_WORK("工程师作业区", "实施工程师可上传，与 Manager 区物理隔离");

    private final String displayName;
    private final String description;

    FolderTypeEnum(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
