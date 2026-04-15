package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ProjectSubtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectSubtaskRepository extends JpaRepository<ProjectSubtask, Long> {
    List<ProjectSubtask> findByProjectIdOrderBySortOrderAscCreatedAtAsc(String projectId);
    long countByProjectIdAndCompletedFalse(String projectId);

    interface PendingSubtaskCountView {
        String getProjectId();
        Long getPendingCount();
    }

    @Query("SELECT s.projectId AS projectId, COUNT(s) AS pendingCount " +
            "FROM ProjectSubtask s " +
            "WHERE s.completed = false AND s.projectId IN :projectIds " +
            "GROUP BY s.projectId")
    List<PendingSubtaskCountView> countPendingByProjectIds(@Param("projectIds") List<String> projectIds);
}
