package com.smartlab.erp.dto;

/**
 * 成员排期 DTO
 * 
 * 包含成员的 userId 和预期的开始/结束日期。
 * 日期使用 ISO-8601 格式字符串传入 (如: "2026-03-15T00:00:00Z")。
 */
public class ScheduleDTO {

    /** 成员用户ID */
    private String userId;

    /** 预期开始日期 (ISO-8601 格式) */
    private String expectedStartDate;

    /** 预期结束日期 (ISO-8601 格式) */
    private String expectedEndDate;

    /** 任务名称 */
    private String taskName;

    /** 预期产出 */
    private String expectedOutput;

    // ===== Getters & Setters =====

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExpectedStartDate() {
        return expectedStartDate;
    }

    public void setExpectedStartDate(String expectedStartDate) {
        this.expectedStartDate = expectedStartDate;
    }

    public String getExpectedEndDate() {
        return expectedEndDate;
    }

    public void setExpectedEndDate(String expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
}
