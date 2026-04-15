package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 中间件分润名册表
 * 固化某个中间件资产的长期分润权利人及其分润比例。
 */
@Entity
@Table(name = "middleware_royalty_roster", indexes = {
        @Index(name = "idx_middleware_user", columnList = "middleware_id,user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiddlewareRoyaltyRoster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的中间件资产
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "middleware_id", nullable = false)
    private MiddlewareAsset middleware;

    /**
     * 分润人用户 ID（User.userId）
     */
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /**
     * 分润比例（0~1 之间的小数，例如 0.25 表示 25%）
     */
    @Column(name = "royalty_ratio", nullable = false, precision = 6, scale = 4)
    private BigDecimal royaltyRatio;
}

