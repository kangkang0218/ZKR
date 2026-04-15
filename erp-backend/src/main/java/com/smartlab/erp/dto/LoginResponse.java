package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String message;
    private UserData user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserData {
        private Long id;
        private String username;
        private String name;
        private String email;
        private String role;
        private String avatar;
    }
}
