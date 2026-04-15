package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 项目里程碑表
 */
@Entity
@Table(name = "project_milestone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "milestone_id")
    private Long milestoneId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private SysProject project;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MilestoneStatus status = MilestoneStatus.PENDING;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "completed_date")
    private Instant completedDate;

    @Column(name = "created_at")
    private Instant createdAt;
}
