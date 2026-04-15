package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ProjectExecutionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectExecutionPlanRepository extends JpaRepository<ProjectExecutionPlan, Long> {

    /**
     * 按项目ID查找实施计划（每个项目最多一份）
     */
    Optional<ProjectExecutionPlan> findByProjectId(String projectId);

    List<ProjectExecutionPlan> findByProjectIdIn(List<String> projectIds);

    /**
     * 检查某项目是否已经有实施计划
     */
    boolean existsByProjectId(String projectId);
}
