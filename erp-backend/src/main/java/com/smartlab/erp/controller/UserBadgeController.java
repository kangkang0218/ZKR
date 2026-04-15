package com.smartlab.erp.controller;

import com.smartlab.erp.dto.AwardBadgeRequest;
import com.smartlab.erp.entity.UserBadge;
import com.smartlab.erp.service.UserBadgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-badges")
@RequiredArgsConstructor
public class UserBadgeController {

    private final UserBadgeService userBadgeService;

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserBadge>> getBadges(@PathVariable String userId) {
        return ResponseEntity.ok(userBadgeService.getBadges(userId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> award(@Valid @RequestBody AwardBadgeRequest request) {
        userBadgeService.awardBadge(request);
        return ResponseEntity.ok(Map.of("success", true, "message", "勋章已发放"));
    }
}
