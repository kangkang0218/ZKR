package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * [扩展表 - Sidecar 模式] 项目成员排期表
 * 
 * 存储 Manager 为每个成员设定的开始/结束日期，
 * 用于计算延误时间和进度跟踪。
 * 
 * 通过 projectId 物理隔离，绝不影响其他项目。
 */
@Entity
@Table(name = "project_member_schedule", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "user_id"}) // 同一项目同一成员只有一条排期
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMemberSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联项目ID (物理隔离键) */
    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    /** 成员用户ID */
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /** 预期开始日期 */
    @Column(name = "expected_start_date")
    private Instant expectedStartDate;

    /** 预期结束日期 */
    @Column(name = "expected_end_date")
    private Instant expectedEndDate;

    /** 实际完成日期 (工程师标记结业时写入) */
    @Column(name = "actual_end_date")
    private Instant actualEndDate;

    /** 任务名称（由 Manager 规划） */
    @Column(name = "task_name", length = 200)
    private String taskName;

    /** 预期产出（由 Manager 规划） */
    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;

    /** 是否已结业 (工程师个人节点) */
    @Column(name = "is_completed")
    @Builder.Default
    private Boolean completed = false;

    /** 是否已由 Manager 确认完成（用于确认参与量） */
    @Column(name = "manager_confirmed")
    @Builder.Default
    private Boolean managerConfirmed = false;

    /** Manager 确认完成时间 */
    @Column(name = "manager_confirmed_at")
    private Instant managerConfirmedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
