package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "internal_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_user_id", nullable = false, length = 64)
    private String recipientUserId;

    @Column(name = "message_type", nullable = false, length = 40)
    private String messageType;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "project_id", length = 64)
    private String projectId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
