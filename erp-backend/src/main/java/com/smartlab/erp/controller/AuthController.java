package com.smartlab.erp.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.smartlab.erp.dto.ChangePasswordRequest;
import com.smartlab.erp.dto.LoginRequest;
import com.smartlab.erp.dto.RegisterRequest;
import com.smartlab.erp.enums.AccountDomain;
import com.smartlab.erp.entity.User;
import com.smartlab.erp.exception.BusinessException;
import com.smartlab.erp.service.AuthService;
import jakarta.validation.Valid; // 🟢 确保引入的是 jakarta.validation
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 遵循 RESTful 标准，对接 AuthService
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * @param request 包含用户名和密码 (已开启 @Valid 校验)
     * @return 包含 Token 的 Map
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        // AuthService.login 负责验证失败抛出 BadCredentialsException，由全局异常处理捕获
        Map<String, String> response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户注册
     * 🟢 修正：
     * 1. 修复了 @Valid 的语法错误
     * 2. 移除了 try-catch，让 Spring Validation 自动处理参数校验失败的情况
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("注册成功，请登录");
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }

    /**
     * 获取当前用户信息
     * 依赖 JWT Filter 设置的 SecurityContext
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    /**
     * 登出
     *由于是无状态 JWT，后端其实不需要做实质操作，前端清除 Token 即可。
     * 保留此接口是为了将来可能的黑名单扩展。
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "请求参数校验失败" : error.getDefaultMessage())
                .orElse("请求参数校验失败");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
        String message = isInvalidDomainValue(ex) ? "账号域取值无效" : "请求体格式错误";
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    private boolean isInvalidDomainValue(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (!(cause instanceof InvalidFormatException invalidFormatException)) {
            return false;
        }
        if (!AccountDomain.class.equals(invalidFormatException.getTargetType())) {
            return false;
        }
        return invalidFormatException.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .anyMatch("domain"::equals);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank() || "Bad credentials".equalsIgnoreCase(message)) {
            message = "用户名、密码或账号域不正确";
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", message));
    }
}
