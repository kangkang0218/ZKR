package com.smartlab.erp.dto;

import com.smartlab.erp.enums.AccountDomain;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    // 🟢 核心：强制校验。如果没有传或为空，Controller层直接拦截，不会进Service
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "角色不能为空")
    private String role;

    @NotNull(message = "账号域不能为空")
    private AccountDomain domain;
}
