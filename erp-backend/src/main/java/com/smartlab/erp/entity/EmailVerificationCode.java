package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "email_verification_codes",
       indexes = {
           @Index(name = "idx_email_code", columnList = "email, code"),
           @Index(name = "idx_email_expires", columnList = "email, expires_at")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "verified", nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
