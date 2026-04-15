package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ProjectAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProjectAssetRepository extends JpaRepository<ProjectAsset, Long> {

    // 🟢 根据项目ID查询所有文件，并按上传时间倒序排列 (最新的在前面)
    List<ProjectAsset> findByProjectProjectIdOrderByUploadedAtDesc(String projectId);

    Optional<ProjectAsset> findByIdAndProjectProjectId(Long id, String projectId);

    /**
     * 统计某个项目在指定分类集合下，已上传的不同 assetCategory 数量
     * 用于 Demo 网关中判断是否集齐 4 类文件。
     */
    @Query("SELECT COUNT(DISTINCT a.assetCategory) " +
           "FROM ProjectAsset a " +
           "WHERE a.project.projectId = :projectId " +
           "  AND a.assetCategory IN :categories")
    long countDistinctAssetCategoryByProjectIdAndCategories(@Param("projectId") String projectId,
                                                            @Param("categories") Set<String> categories);
}
