package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 科研流项目扩展表（Sidecar）
 * 存放科研专用的信息和状态，避免侵入 SysProject 主结构。
 */
@Entity
@Table(name = "research_project_profile", indexes = {
        @Index(name = "idx_research_project_id", columnList = "project_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResearchProjectProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的 SysProject.projectId（弱关联，不加外键约束）
     */
    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    /**
     * 科研流当前状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "research_status", nullable = false, length = 40)
    private ResearchStatus status;

    /**
     * 初探阶段的创新点说明
     */
    @Column(name = "innovation_point", columnDefinition = "TEXT")
    private String innovationPoint;

    @Column(name = "idea_text", columnDefinition = "TEXT")
    private String ideaText;

    @Column(name = "budget_estimate", precision = 19, scale = 4)
    private BigDecimal budgetEstimate;

    @Column(name = "idea_owner_user_id", length = 64)
    private String ideaOwnerUserId;

    @Column(name = "host_user_id", length = 64)
    private String hostUserId;

    @Column(name = "chief_engineer_user_id", length = 64)
    private String chiefEngineerUserId;

    @Column(name = "blueprint_owner_user_id", length = 64)
    private String blueprintOwnerUserId;

    @Column(name = "architecture_owner_user_id", length = 64)
    private String architectureOwnerUserId;

    @Column(name = "task_breakdown_owner_user_id", length = 64)
    private String taskBreakdownOwnerUserId;

    @Column(name = "evaluation_report_owner_user_id", length = 64)
    private String evaluationReportOwnerUserId;

    /**
     * 施工执行阶段的执行模式：
     * - MODE_A_PARALLEL：平行缝合模式
     * - MODE_B_ITERATIVE：单体迭代模式
     */
    @Column(name = "execution_mode", length = 40)
    private String executionMode;

    @Column(name = "workflow_flags", columnDefinition = "TEXT")
    private String workflowFlags;
}
