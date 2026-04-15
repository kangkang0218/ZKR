package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ProjectChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectChatMessageRepository extends JpaRepository<ProjectChatMessage, Long> {
    List<ProjectChatMessage> findTop100ByProjectIdOrderByIdDesc(String projectId);
    List<ProjectChatMessage> findTop100ByProjectIdAndStageTagOrderByIdDesc(String projectId, String stageTag);
    long countDistinctSenderUserIdByProjectId(String projectId);
    long countDistinctSenderUserIdByProjectIdAndStageTag(String projectId, String stageTag);
}
