package com.smartlab.erp.repository;

import com.smartlab.erp.entity.SysProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SysProjectMemberRepository extends JpaRepository<SysProjectMember, Long> {

    List<SysProjectMember> findByProjectId(String projectId);

    @Query("SELECT m FROM SysProjectMember m JOIN FETCH m.user WHERE m.projectId = :projectId")
    List<SysProjectMember> findByProjectIdWithUser(@Param("projectId") String projectId);

    @Query("SELECT m FROM SysProjectMember m JOIN FETCH m.user WHERE m.projectId IN :projectIds")
    List<SysProjectMember> findByProjectIdInWithUser(@Param("projectIds") List<String> projectIds);

    @Query("SELECT m FROM SysProjectMember m JOIN FETCH m.user WHERE m.user.userId = :userId")
    List<SysProjectMember> findByUserIdWithUser(@Param("userId") String userId);

    Optional<SysProjectMember> findByProjectIdAndUserUserId(String projectId, String userId);

    boolean existsByProjectIdAndUserUserId(String projectId, String userId);

    @Query("SELECT pm FROM SysProjectMember pm JOIN FETCH pm.user WHERE pm.user.userId IN :userIds")
    List<SysProjectMember> findByUserIdInWithUser(@Param("userIds") List<String> userIds);

    @Modifying
    void deleteByProjectId(String projectId);

    @Modifying
    long deleteByProjectIdAndUserUserId(String projectId, String userId);
}
