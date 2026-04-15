package com.smartlab.erp.repository;

import com.smartlab.erp.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    Optional<EmailVerificationCode> findByEmailAndCodeAndExpiresAtAfter(String email, String code, Instant now);

    List<EmailVerificationCode> findByEmailAndExpiresAtAfter(String email, Instant now);

    @Modifying
    @Query("DELETE FROM EmailVerificationCode e WHERE e.expiresAt < :now")
    void deleteAllExpired(Instant now);

    @Modifying
    @Query("DELETE FROM EmailVerificationCode e WHERE e.email = :email AND e.verified = false")
    void deleteUnverifiedByEmail(String email);
}
