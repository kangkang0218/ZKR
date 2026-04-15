package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "project_subtask")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectSubtask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "assignee_user_id", length = 64)
    private String assigneeUserId;

    @Column(name = "assignee_name", length = 120)
    private String assigneeName;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
