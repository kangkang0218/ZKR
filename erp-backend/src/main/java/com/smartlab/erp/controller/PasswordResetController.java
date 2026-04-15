package com.smartlab.erp.controller;

import com.smartlab.erp.dto.EmailVerificationRequest;
import com.smartlab.erp.dto.ResetPasswordRequest;
import com.smartlab.erp.dto.VerifyCodeRequest;
import com.smartlab.erp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final AuthService authService;

    /**
     * 发送验证码到邮箱
     */
    @PostMapping("/send-code")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@Valid @RequestBody EmailVerificationRequest request) {
        authService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "验证码已发送"));
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, String>> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "验证码验证成功"));
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "密码重置成功"));
    }
}
