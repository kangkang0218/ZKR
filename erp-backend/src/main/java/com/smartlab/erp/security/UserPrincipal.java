package com.smartlab.erp.security;

import com.smartlab.erp.enums.AccountDomain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 用户主体类 - 封装用户认证信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private String userId;
    private String username;
    private String name;
    private String role;
    private String email;
    private AccountDomain accountDomain;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String userId, String username, String name, String role, String email,
                         Collection<? extends GrantedAuthority> authorities) {
        this(userId, username, name, role, email, AccountDomain.ERP, authorities);
    }

    // ✅ 新增：为了兼容 ProjectService 中的 .getId() 调用
    // Lombok 默认生成的是 getUserId()，我们手动加一个别名
    public String getId() {
        return userId;
    }

    // --- UserDetails 接口实现 ---

    @Override
    public String getPassword() {
        return null; // JWT场景通常不需要在Principal中保留密码
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
