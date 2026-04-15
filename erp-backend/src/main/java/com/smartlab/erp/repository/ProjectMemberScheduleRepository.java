package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ProjectMemberSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberScheduleRepository extends JpaRepository<ProjectMemberSchedule, Long> {

    /**
     * 查询某项目的所有成员排期
     */
    List<ProjectMemberSchedule> findByProjectId(String projectId);

    /**
     * 查询某项目某成员的排期
     */
    Optional<ProjectMemberSchedule> findByProjectIdAndUserId(String projectId, String userId);

    List<ProjectMemberSchedule> findByProjectIdIn(List<String> projectIds);

    /**
     * 删除某项目的所有排期（用于重新设定时先清除旧数据）
     */
    void deleteByProjectId(String projectId);

    void deleteByProjectIdAndUserId(String projectId, String userId);
}
