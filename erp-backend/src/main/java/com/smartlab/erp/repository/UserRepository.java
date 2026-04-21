package com.smartlab.erp.repository;

import com.smartlab.erp.entity.User;
import com.smartlab.erp.enums.AccountDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT MAX(u.userId) FROM User u")
    Optional<String> findMaxUserId();

    Optional<User> findByEmailAndAccountDomain(String email, AccountDomain accountDomain);

    List<User> findByRoleAndAccountDomain(String role, AccountDomain accountDomain);

    Optional<User> findByRoleAndIsLeaderTrueAndAccountDomain(String role, AccountDomain accountDomain);
}
