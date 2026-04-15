package com.smartlab.erp.security;

import com.smartlab.erp.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

/**
 * 用户详情服务
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 这里我们使用JWT中的信息创建UserDetails
        // 实际应用中应该从数据库加载用户信息
        Map<String, Object> userInfo = jwtUtil.getUserInfoFromToken(
                SecurityContextHolder.getContext().getAuthentication()
                        .getCredentials().toString()
        );

        if (userInfo.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new UserPrincipal(
                (String) userInfo.get("userId"),
                username,
                (String) userInfo.get("name"),
                (String) userInfo.get("role"),
                (String) userInfo.get("email"),
                new ArrayList<>()
        );
    }
}
