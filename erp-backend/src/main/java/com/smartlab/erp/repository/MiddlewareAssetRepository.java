package com.smartlab.erp.repository;

import com.smartlab.erp.entity.MiddlewareAsset;
import com.smartlab.erp.entity.FlowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MiddlewareAssetRepository extends JpaRepository<MiddlewareAsset, Long> {
    List<MiddlewareAsset> findBySourceFlowTypeOrderByCreatedAtDesc(FlowType flowType);
    List<MiddlewareAsset> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);
}
