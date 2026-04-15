package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 中间件资产表（Sidecar）
 * 用于记录由科研流评测入库后形成的内部中间件资产。
 */
@Entity
@Table(name = "middleware_asset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiddlewareAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 中间件名称
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 功能描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 溯源科研项目 ID（SysProject.projectId）
     */
    @Column(name = "source_project_id", nullable = false, length = 64)
    private String sourceProjectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_flow_type", length = 20)
    private FlowType sourceFlowType;

    @Column(name = "source_status", length = 40)
    private String sourceStatus;

    @Column(name = "owner_user_id", length = 64)
    private String ownerUserId;

    /**
     * 中间件代码仓库 / 归档地址
     */
    @Column(name = "repo_url", length = 500)
    private String repoUrl;

    /**
     * 评级（默认 A 级，可扩展为 S/A/B/C）
     */
    @Column(name = "rating", length = 10)
    private String rating;

    @Column(name = "pricing_model", length = 40)
    private String pricingModel;

    @Column(name = "unit_price", precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "internal_cost_price", precision = 19, scale = 4)
    private BigDecimal internalCostPrice;

    @Column(name = "market_reference_price", precision = 19, scale = 4)
    private BigDecimal marketReferencePrice;

    @Column(name = "currency", length = 16)
    private String currency;

    @Column(name = "billing_unit", length = 30)
    private String billingUnit;

    @Column(name = "version_tag", length = 80)
    private String versionTag;

    @Column(name = "lifecycle_status", length = 30)
    private String lifecycleStatus;

    @Column(name = "extra_metadata", columnDefinition = "TEXT")
    private String extraMetadata;

    /**
     * 创建时间（用于审计）
     */
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.rating == null) {
            this.rating = "A";
        }
        if (this.currency == null || this.currency.isBlank()) {
            this.currency = "CNY";
        }
        if (this.pricingModel == null || this.pricingModel.isBlank()) {
            this.pricingModel = "INTERNAL";
        }
        if (this.billingUnit == null || this.billingUnit.isBlank()) {
            this.billingUnit = "PROJECT";
        }
        if (this.lifecycleStatus == null || this.lifecycleStatus.isBlank()) {
            this.lifecycleStatus = "DRAFT";
        }
    }
}
