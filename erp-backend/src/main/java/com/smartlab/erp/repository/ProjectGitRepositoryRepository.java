package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ProjectGitRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectGitRepositoryRepository extends JpaRepository<ProjectGitRepository, Long> {
    List<ProjectGitRepository> findByProjectIdAndActiveTrueOrderByCreatedAtDesc(String projectId);
    Optional<ProjectGitRepository> findByIdAndProjectIdAndActiveTrue(Long id, String projectId);
}
