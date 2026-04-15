package com.smartlab.erp.finance.entity;

import com.smartlab.erp.entity.SysProject;
import com.smartlab.erp.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "finance_venture_equity", indexes = {
        @Index(name = "idx_finance_equity_project_user", columnList = "project_id,user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceVentureEquity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private SysProject project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "equity_ratio", nullable = false, precision = 6, scale = 4)
    private BigDecimal equityRatio;

    @Column(name = "dividend_ratio", precision = 6, scale = 4)
    private BigDecimal dividendRatio;

    @Column(name = "role_code", length = 50)
    private String roleCode;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = Boolean.TRUE;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
