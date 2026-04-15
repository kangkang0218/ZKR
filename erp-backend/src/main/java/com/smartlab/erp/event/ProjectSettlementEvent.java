package com.smartlab.erp.event;

import org.springframework.context.ApplicationEvent;

/**
 * 项目结算归档事件
 * 
 * 当项目 OCR 凭证校验通过、状态跃迁至 SETTLEMENT 后触发此事件。
 * Listener 可以异步处理文件归档、通知下游系统等操作。
 */
public class ProjectSettlementEvent extends ApplicationEvent {

    /** 项目ID */
    private final String projectId;

    /** 操作人ID (Manager) */
    private final String operatorUserId;

    /** 凭证文件路径 */
    private final String voucherFilePath;

    public ProjectSettlementEvent(Object source, String projectId, String operatorUserId, String voucherFilePath) {
        super(source);
        this.projectId = projectId;
        this.operatorUserId = operatorUserId;
        this.voucherFilePath = voucherFilePath;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getOperatorUserId() {
        return operatorUserId;
    }

    public String getVoucherFilePath() {
        return voucherFilePath;
    }
}
