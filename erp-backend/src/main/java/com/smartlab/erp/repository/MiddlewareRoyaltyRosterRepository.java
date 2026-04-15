package com.smartlab.erp.repository;

import com.smartlab.erp.entity.MiddlewareRoyaltyRoster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MiddlewareRoyaltyRosterRepository extends JpaRepository<MiddlewareRoyaltyRoster, Long> {
    List<MiddlewareRoyaltyRoster> findByMiddleware_IdOrderByIdAsc(Long middlewareId);
}
