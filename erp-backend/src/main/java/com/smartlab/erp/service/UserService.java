package com.smartlab.erp.service;

import com.smartlab.erp.entity.User;
import com.smartlab.erp.repository.UserBadgeRepository;
import com.smartlab.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户业务逻辑层
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;

    /**
     * ✅ 查询所有用户
     * 用于前端"新建项目"时的团队成员选择
     */
    public List<User> findAllUsers() {
        return userRepository.findAll().stream().peek(this::enrichUser).toList();
    }

    /**
     * 更新用户头像
     */
    @Transactional
    public void updateAvatar(String userId, String avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatar(avatar);
        userRepository.save(user);
    }

    public User enrichUser(User user) {
        user.setBadges(userBadgeRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId()));
        return user;
    }
}
