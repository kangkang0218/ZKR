package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * [Smart Lab v2.0] 项目成员关联表
 * 对应数据库表: sys_project_member
 * 作用：解决“用户无法看到自己被拉入的项目”的关键关联表
 */
@Entity
@Table(name = "sys_project_member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "user_id"}) // 防止重复拉人
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🟢 关联项目 (String ID)
    // 这里的 name="project_id" 对应数据库的外键列
    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    // 🟢 关联用户 (String ID)
    // 直接存 ID，或者用 @ManyToOne 关联 User 对象都可以
    // 这里为了查询方便（比如查成员名字），建议关联 User 对象
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 🟢 成员角色 (MEMBER, ADMIN, VIEWER)
    @Column(length = 20)
    @Builder.Default
    private String role = "MEMBER";
    @Column(name = "weight")
    @Builder.Default
    private Integer weight = 0;

    @Column(name = "manager_weight")
    @Builder.Default
    private Integer managerWeight = 0;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = Instant.now();
    }
}
