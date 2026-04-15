package com.smartlab.erp.service;

import com.smartlab.erp.dto.AwardBadgeRequest;
import com.smartlab.erp.entity.User;
import com.smartlab.erp.entity.UserBadge;
import com.smartlab.erp.exception.BusinessException;
import com.smartlab.erp.exception.PermissionDeniedException;
import com.smartlab.erp.repository.UserBadgeRepository;
import com.smartlab.erp.repository.UserRepository;
import com.smartlab.erp.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBadgeService {

    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    @Value("${auth.provisioning.admin-user-id:}")
    private String adminUserId;

    @Transactional
    public void awardBadge(AwardBadgeRequest request) {
        var current = AuthUtils.getCurrentUserPrincipal();
        if (current == null) {
            throw new PermissionDeniedException("用户未登录或会话已过期");
        }
        if (adminUserId != null && !adminUserId.isBlank() && !adminUserId.equals(current.getId())) {
            throw new PermissionDeniedException("仅管理员可发放勋章");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        userBadgeRepository.save(UserBadge.builder()
                .userId(user.getUserId())
                .badgeName(request.getBadgeName())
                .badgeIcon(request.getBadgeIcon())
                .badgeColor(request.getBadgeColor())
                .awardedBy(current.getId())
                .build());
        if (request.getHiddenAvatar() != null) {
            user.setHiddenAvatar(request.getHiddenAvatar());
            userRepository.save(user);
        }
    }

    @Transactional(readOnly = true)
    public List<UserBadge> getBadges(String userId) {
        return userBadgeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
