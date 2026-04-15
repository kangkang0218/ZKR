package com.smartlab.erp.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "finance_knowledge_document", indexes = {
        @Index(name = "idx_finance_knowledge_source", columnList = "source_table,source_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceKnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    @Column(name = "source_table", length = 100)
    private String sourceTable;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "embedding_ref", length = 200)
    private String embeddingRef;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = Boolean.TRUE;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
