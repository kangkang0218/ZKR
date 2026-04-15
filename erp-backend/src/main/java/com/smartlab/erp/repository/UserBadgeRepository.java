package com.smartlab.erp.repository;

import com.smartlab.erp.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserIdOrderByCreatedAtDesc(String userId);
}
