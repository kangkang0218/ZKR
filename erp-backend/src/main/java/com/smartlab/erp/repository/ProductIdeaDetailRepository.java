package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ProductIdeaDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProductIdeaDetailRepository extends JpaRepository<ProductIdeaDetail, Long> {

    Optional<ProductIdeaDetail> findByProjectId(String projectId);

    List<ProductIdeaDetail> findByProjectIdIn(List<String> projectIds);
}
