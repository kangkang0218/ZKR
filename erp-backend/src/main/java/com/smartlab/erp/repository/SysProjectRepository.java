package com.smartlab.erp.repository;

import com.smartlab.erp.entity.FlowType;
import com.smartlab.erp.entity.ProjectStatus;
import com.smartlab.erp.entity.SysProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysProjectRepository extends JpaRepository<SysProject, String> {

    /**
     * 🟢 场景1：Manager Dashboard (管理仪表盘)
     * 规则：仅查询“我是发起者(BD/BUSINESS)或当前负责人(manager)”的项目
     */
    @Query("SELECT p FROM SysProject p " +
            "WHERE p.manager.userId = :userId " +
            "   OR EXISTS (" +
            "       SELECT 1 FROM SysProjectMember m " +
            "       WHERE m.projectId = p.projectId " +
            "         AND m.user.userId = :userId " +
            "         AND UPPER(m.role) IN ('BD', 'BUSINESS')" +
            "   ) " +
            "ORDER BY p.createdAt DESC")
    List<SysProject> findManagedProjects(@Param("userId") String userId);

    /**
     * 🟢 场景2：Workspace (工作区)
     * 规则：仅查询“我是成员”的项目 (通过 JOIN 成员表)
     * 修复Bug：明确关联 sys_project_member 表，只要被邀请了，一定能查到
     */
    @Query("SELECT p FROM SysProject p " +
            "WHERE EXISTS (" +
            "       SELECT 1 FROM SysProjectMember m " +
            "       WHERE m.projectId = p.projectId " +
            "         AND m.user.userId = :userId" +
            "   ) " +
            "ORDER BY p.updatedAt DESC, p.createdAt DESC")
    List<SysProject> findParticipatedProjects(@Param("userId") String userId);

    /**
     * ✅ 安全查询：获取单个项目详情
     * 逻辑：只有相关人员 (负责人或成员) 才能查到数据，防止通过 ID 越权访问
     */
    @Query("SELECT p FROM SysProject p " +
            "WHERE p.projectId = :projectId " +
            "  AND (p.manager.userId = :userId " +
            "       OR EXISTS (" +
            "           SELECT 1 FROM SysProjectMember m " +
            "           WHERE m.projectId = p.projectId " +
            "             AND m.user.userId = :userId" +
            "       ))")
    Optional<SysProject> findProjectByIdAndUser(@Param("projectId") String projectId,
                                                @Param("userId") String userId);

    /**
     * ✅ 权限检查：是否是负责人 (Manager)
     * 用途：RbacService 中用于判断是否有“写/删除”权限
     */
    @Query("SELECT COUNT(p) > 0 FROM SysProject p " +
            "WHERE p.projectId = :projectId AND p.manager.userId = :userId")
    boolean isManager(@Param("projectId") String projectId,
                      @Param("userId") String userId);

    List<SysProject> findByFlowTypeAndProjectStatusNotOrderByUpdatedAtDescCreatedAtDesc(FlowType flowType,
                                                                                         ProjectStatus projectStatus);
}
