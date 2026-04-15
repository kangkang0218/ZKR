package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "project_chat_message", indexes = {
        @Index(name = "idx_project_chat_project", columnList = "project_id"),
        @Index(name = "idx_project_chat_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    @Column(name = "sender_user_id", nullable = false, length = 64)
    private String senderUserId;

    @Column(name = "sender_name", nullable = false, length = 120)
    private String senderName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "stage_tag", length = 40)
    private String stageTag;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
