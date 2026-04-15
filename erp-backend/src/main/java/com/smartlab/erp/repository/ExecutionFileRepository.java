package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ExecutionFile;
import com.smartlab.erp.enums.FolderTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExecutionFileRepository extends JpaRepository<ExecutionFile, Long> {

    /**
     * 查询某项目某隔离区的所有文件（双盲查询的基础方法）
     * 按上传时间倒序排列
     */
    List<ExecutionFile> findByProjectIdAndFolderTypeOrderByUploadedAtDesc(String projectId, FolderTypeEnum folderType);

    /**
     * 查询某项目某用户上传的文件
     */
    List<ExecutionFile> findByProjectIdAndUploaderUserIdOrderByUploadedAtDesc(String projectId, String uploaderUserId);

    Optional<ExecutionFile> findByIdAndProjectId(Long id, String projectId);
}
