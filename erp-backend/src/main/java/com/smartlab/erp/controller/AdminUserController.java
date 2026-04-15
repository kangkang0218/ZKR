package com.smartlab.erp.controller;

import com.smartlab.erp.dto.ProvisionUserRequest;
import com.smartlab.erp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuthService authService;

    @PostMapping("/provision")
    public ResponseEntity<Map<String, String>> provisionUser(@Valid @RequestBody ProvisionUserRequest request) {
        authService.provisionUser(request);
        return ResponseEntity.ok(Map.of("message", "账号创建成功，初始密码为：账号+123"));
    }
}
