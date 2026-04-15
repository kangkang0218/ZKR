package com.smartlab.erp.config;

import com.smartlab.erp.enums.AccountDomain;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * ✅ 已修正：全量适配 String 类型的 UserID
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Token
     * 🟢 修正：userId 改为 String
     */
    public String generateToken(String userId, String username, String name, String role, String email) {
        return generateToken(userId, username, name, role, email, AccountDomain.ERP);
    }

    public String generateToken(String userId, String username, String name, String role, String email,
                                AccountDomain accountDomain) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId); // 存入 String
        claims.put("username", username);
        claims.put("name", name);
        claims.put("role", role);
        claims.put("email", email);
        claims.put("accountDomain", accountDomain == null ? AccountDomain.ERP.name() : accountDomain.name());

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中提取Claims
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 提取用户ID
     * 🟢 修正：返回 String
     */
    public String extractUserId(String token) {
        // 从 Claims 里拿出来可能是 String 也可能是 Integer，统一转 String 最安全
        Object idObj = extractClaims(token).get("userId");
        return idObj != null ? String.valueOf(idObj) : null;
    }

    /**
     * 提取用户名
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * 提取角色
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * 验证Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证Token有效性
     */
    public boolean validateToken(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从Token中获取所有用户信息（用于认证过滤器）
     * 🟢 修正：userId 统一按 String 处理
     */
    public Map<String, Object> getUserInfoFromToken(String token) {
        Claims claims = extractClaims(token);
        Map<String, Object> userInfo = new HashMap<>();

        // 🟢 核心修正：不管 Token 里存的是 1 还是 "1"，出来都是 "1"
        Object idObj = claims.get("userId");
        userInfo.put("userId", idObj != null ? String.valueOf(idObj) : null);

        userInfo.put("username", claims.getSubject());
        userInfo.put("name", claims.get("name", String.class));
        userInfo.put("role", claims.get("role", String.class));
        userInfo.put("email", claims.get("email", String.class));
        userInfo.put("accountDomain", claims.get("accountDomain", String.class));
        return userInfo;
    }
}
