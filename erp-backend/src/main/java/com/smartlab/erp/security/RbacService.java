package com.smartlab.erp.security;

import com.smartlab.erp.entity.SysProject;
import com.smartlab.erp.entity.SysProjectMember;
import com.smartlab.erp.entity.ProjectStatus;
import com.smartlab.erp.entity.ProductStatus;
import com.smartlab.erp.entity.FlowType;
import com.smartlab.erp.repository.SysProjectMemberRepository;
import com.smartlab.erp.repository.SysProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * [Smart Lab v3.0] 动态 RBAC 权限服务
 * ✅ 已适配三流并行架构 (Project/Product/Research)
 */
@Component
@RequiredArgsConstructor
public class RbacService {

    private final SysProjectRepository projectRepository;
    private final SysProjectMemberRepository projectMemberRepository;

    @Value("${auth.admin-usernames:Zhangqi,guojianwen,jiaomiao}")
    private String adminUsernamesConfig;

    private Set<String> getAdminUsernames() {
        return Set.of(adminUsernamesConfig.split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private boolean isAdminUser(String role, String username) {
        if (role != null && "ADMIN".equalsIgnoreCase(role.trim())) {
            return true;
        }
        return username != null && getAdminUsernames().contains(username.trim());
    }

    public enum Permission {
        READ, WRITE, BUDGET, ASSIGN, DELETE, UPLOAD, TRANSITION_STATUS
    }

    public boolean isOwner(String projectId, String userId) {
        return projectRepository.isManager(projectId, userId);
    }

    public boolean canManageBudget(String projectId, String userId) {
        return isOwner(projectId, userId);
    }

    /**
     * ✅ 核心逻辑：判断项目是否已结项
     * 适配三流架构，防止 NullPointerException
     */
    private boolean isProjectCompleted(SysProject p) {
        if (p == null || p.getFlowType() == null) {
            return false;
        }

        // 🟢 根据流程类型进行状态判定
        return switch (p.getFlowType()) {
            case PROJECT -> p.getProjectStatus() == ProjectStatus.COMPLETED;
            case PRODUCT -> p.getProductStatus() == ProductStatus.SHELVED || p.getProductStatus() == ProductStatus.LAUNCHED;
            case RESEARCH -> false; // 科研流暂无完结状态
        };
    }

    /**
     * ✅ 核心权限检查方法
     */
    public boolean hasPermission(String projectId, Permission permission, String userId, String userRole) {
        if (isAdminUser(userRole, null)) {
            return true;
        }

        if (isOwner(projectId, userId)) {
            // 负责人权限：除了“已完结项目”的预算修改权限被锁定外，拥有所有权限
            if (permission == Permission.BUDGET) {
                return projectRepository.findById(projectId)
                        .map(p -> !isProjectCompleted(p)) // 🟢 替换了原有的 p.getStatus()
                        .orElse(false);
            }
            return true;
        }

        // 普通成员权限检查
        // 注意：建议在 Repo 中实现此方法以优化性能，当前为 Stream 内存过滤
        Optional<SysProjectMember> memberOpt = projectMemberRepository.findAll().stream()
                .filter(m -> m.getProjectId().equals(projectId) && m.getUser().getUserId().equals(userId))
                .findFirst();

        if (memberOpt.isEmpty()) {
            return false;
        }

        return checkMemberPermission(memberOpt.get().getRole(), permission);
    }

    private boolean checkMemberPermission(String role, Permission permission) {
        if (role == null) return false;

        return switch (normalizeMemberRole(role)) {
            case "PM", "LEAD" ->
                    List.of(Permission.READ, Permission.WRITE, Permission.ASSIGN).contains(permission);
            case "DEV", "ALGORITHM", "DATA", "QA" ->
                    List.of(Permission.READ, Permission.UPLOAD).contains(permission);
            case "BUSINESS", "VIEWER" ->
                    permission == Permission.READ;
            default ->
                    permission == Permission.READ;
        };
    }

    private String normalizeMemberRole(String role) {
        String normalized = role.toUpperCase(Locale.ROOT);
        if ("ALGO".equals(normalized)) {
            return "ALGORITHM";
        }
        if ("REASE".equals(normalized)) {
            return "RESEARCH";
        }
        return normalized;
    }

    public void denyAccess(String message) {
        throw new AccessDeniedException(message);
    }
}
