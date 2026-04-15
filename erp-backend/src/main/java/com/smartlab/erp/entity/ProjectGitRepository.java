package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "project_git_repository")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectGitRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    @Column(name = "repository_url", nullable = false, length = 500)
    private String repositoryUrl;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "branch", length = 120)
    private String branch;

    @Column(name = "provider", length = 40)
    private String provider;

    @Column(name = "created_by_user_id", nullable = false, length = 64)
    private String createdByUserId;

    @Column(name = "last_test_status", length = 30)
    private String lastTestStatus;

    @Column(name = "last_test_message", columnDefinition = "TEXT")
    private String lastTestMessage;

    @Column(name = "last_tested_at")
    private Instant lastTestedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
        if (this.provider == null || this.provider.isBlank()) {
            this.provider = "GITHUB";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
