package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_badge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "badge_name", nullable = false, length = 80)
    private String badgeName;

    @Column(name = "badge_icon", length = 20)
    private String badgeIcon;

    @Column(name = "badge_color", length = 20)
    private String badgeColor;

    @Column(name = "awarded_by", length = 64)
    private String awardedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
