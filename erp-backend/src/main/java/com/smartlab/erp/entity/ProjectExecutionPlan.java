package com.smartlab.erp.entity;

import com.smartlab.erp.enums.ProjectTierEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * [扩展表 - Sidecar 模式] 项目实施计划表
 * 
 * 用于存储实施阶段(EXECUTION)的计划信息：
 * - 难度分级 (difficultyLevel)
 * - 技术栈描述 (techStackDescription)
 * 
 * 通过 projectId 物理隔离，绝不影响其他项目。
 * 不侵入主实体 SysProject。
 */
@Entity
@Table(name = "project_execution_plan", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id"}) // 每个项目仅有一份实施计划
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectExecutionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联项目ID (物理隔离键) */
    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    /** 预评估项目难度分级 (如: 简单/中等/困难/极限) */
    @Column(name = "difficulty_level", length = 50)
    private String difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_tier", length = 10)
    private ProjectTierEnum projectTier;

    @Column(name = "goal_description", columnDefinition = "TEXT")
    private String goalDescription;

    /** 技术栈和深度描述 */
    @Column(name = "tech_stack_description", columnDefinition = "TEXT")
    private String techStackDescription;

    /** 创建人 (Manager) ID */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
