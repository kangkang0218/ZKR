package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 产品流扩展表：存放 Idea 阶段的长文本与主观描述
 * 通过 projectId 与 SysProject 做弱关联，避免污染主实体结构。
 */
@Entity
@Table(name = "product_idea_detail", indexes = {
        @Index(name = "idx_product_idea_project", columnList = "project_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductIdeaDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的 SysProject.projectId（弱关联，不加外键约束）
     */
    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    /**
     * 目标用户 / 受众
     */
    @Column(name = "target_users", columnDefinition = "TEXT")
    private String targetUsers;

    /**
     * 核心特性 / 卖点
     */
    @Column(name = "core_features", columnDefinition = "TEXT")
    private String coreFeatures;

    /**
     * 主要用途
     */
    @Column(name = "use_case", columnDefinition = "TEXT")
    private String useCase;

    /**
     * 要解决的问题
     */
    @Column(name = "problem_statement", columnDefinition = "TEXT")
    private String problemStatement;

    /**
     * 技术栈与实现思路描述
     */
    @Column(name = "tech_stack_desc", columnDefinition = "TEXT")
    private String techStackDesc;

    /**
     * Idea 主理人（发起人）
     */
    @Column(name = "idea_owner_user_id", length = 64)
    private String ideaOwnerUserId;

    /**
     * 推广执行人
     */
    @Column(name = "promotion_ic_user_id", length = 64)
    private String promotionIcUserId;

    /**
     * 会议参会成员（userId 列表，逗号分隔）
     */
    @Column(name = "meeting_participant_user_ids", columnDefinition = "TEXT")
    private String meetingParticipantUserIds;

    /**
     * 测试阶段的整体反馈（由推广 IC 提交）
     */
    @Column(name = "test_feedback", columnDefinition = "TEXT")
    private String testFeedback;

    @Column(name = "demo_engineering_owner_user_id", length = 64)
    private String demoEngineeringOwnerUserId;

    @Column(name = "demo_file_owner_user_id", length = 64)
    private String demoFileOwnerUserId;

    @Column(name = "demo_description_owner_user_id", length = 64)
    private String demoDescriptionOwnerUserId;

    @Column(name = "demo_feasibility_owner_user_id", length = 64)
    private String demoFeasibilityOwnerUserId;
}
