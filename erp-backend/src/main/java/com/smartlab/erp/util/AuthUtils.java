package com.smartlab.erp.util;

import com.smartlab.erp.enums.BusinessRoleEnum;
import com.smartlab.erp.exception.PermissionDeniedException;
import com.smartlab.erp.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限工具类，用于获取当前登录用户及进行权限校验。
 * 在实际生产环境中，这部分逻辑通常会与 Spring Security 深度集成。
 */
public class AuthUtils {

    /**
     * 获取当前登录用户的 UserPrincipal。
     * @return 当前登录用户的 UserPrincipal，如果未登录则返回 null。
     */
    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户的 ID。
     * @return 用户 ID，如果未登录则抛出异常。
     * @throws PermissionDeniedException 如果用户未登录。
     */
    public static String getCurrentUserId() {
        UserPrincipal userPrincipal = getCurrentUserPrincipal();
        if (userPrincipal == null) {
            throw new PermissionDeniedException("用户未登录");
        }
        return userPrincipal.getId();
    }

    /**
     * 获取当前登录用户的角色列表。
     * 注意：这里的角色是 Spring Security 的 GrantedAuthority 形式，会带有 "ROLE_" 前缀。
     * @return 角色字符串列表。
     */
    public static List<String> getCurrentUserRoles() {
        UserPrincipal userPrincipal = getCurrentUserPrincipal();
        if (userPrincipal == null) {
            return List.of();
        }
        return userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(s -> s.replace("ROLE_", "")) // 移除 ROLE_ 前缀
                .collect(Collectors.toList());
    }

    /**
     * 校验当前用户是否包含指定业务角色。
     * @param requiredRole 必需的业务角色。
     * @throws PermissionDeniedException 如果用户未登录或不具备所需角色。
     */
    public static void checkCurrentUserHasRole(BusinessRoleEnum requiredRole) {
        List<String> userRoles = getCurrentUserRoles();
        if (!userRoles.contains(requiredRole.name())) {
            throw new PermissionDeniedException("权限不足：需要角色 " + requiredRole.getDescription());
        }
    }

    /**
     * 校验当前用户是否包含指定业务角色之一。
     * @param requiredRoles 必需的业务角色列表。
     * @throws PermissionDeniedException 如果用户未登录或不具备所需角色。
     */
    public static void checkCurrentUserHasAnyRole(BusinessRoleEnum... requiredRoles) {
        List<String> userRoles = getCurrentUserRoles();
        boolean hasAnyRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> userRoles.contains(role.name()));

        if (!hasAnyRole) {
            String roleNames = Arrays.stream(requiredRoles)
                    .map(BusinessRoleEnum::getDescription)
                    .collect(Collectors.joining(", "));
            throw new PermissionDeniedException("权限不足：需要角色 " + roleNames);
        }
    }
}
