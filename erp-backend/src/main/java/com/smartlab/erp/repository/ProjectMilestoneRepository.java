package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * [Smart Lab v2.0] 里程碑数据访问层
 */
@Repository
public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone, Long> {

    // 🟢 根据项目 ID 获取所有里程碑，并按时间排序
    List<ProjectMilestone> findByProjectProjectIdOrderByDueDateAsc(String projectId);
}