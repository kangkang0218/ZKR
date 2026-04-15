package com.smartlab.erp.finance.entity;

import com.smartlab.erp.entity.SysProject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "finance_venture_profile", indexes = {
        @Index(name = "idx_finance_venture_profile_project", columnList = "project_id", unique = true),
        @Index(name = "idx_finance_venture_profile_legacy", columnList = "legacy_venture_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceVentureProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private SysProject project;

    @Column(name = "legacy_venture_id", nullable = false)
    private Long legacyVentureId;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "legacy_stage", length = 100)
    private String legacyStage;

    @Column(name = "ledger_enabled", nullable = false)
    @Builder.Default
    private Boolean ledgerEnabled = Boolean.TRUE;

    @Column(name = "source_system", length = 50)
    private String sourceSystem;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (sourceSystem == null) {
            sourceSystem = "legacy-finance";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
