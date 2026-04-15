package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ResearchProjectProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ResearchProjectProfileRepository extends JpaRepository<ResearchProjectProfile, Long> {

    Optional<ResearchProjectProfile> findByProjectId(String projectId);

    List<ResearchProjectProfile> findByProjectIdIn(List<String> projectIds);
}
