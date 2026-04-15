package com.smartlab.erp.repository;

import com.smartlab.erp.entity.ExecutionArchiveFolder;
import com.smartlab.erp.enums.FolderTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExecutionArchiveFolderRepository extends JpaRepository<ExecutionArchiveFolder, Long> {

    List<ExecutionArchiveFolder> findByProjectIdAndFolderTypeOrderByFolderPathAsc(String projectId, FolderTypeEnum folderType);

    Optional<ExecutionArchiveFolder> findByProjectIdAndFolderTypeAndFolderPath(String projectId, FolderTypeEnum folderType, String folderPath);

    boolean existsByProjectIdAndFolderTypeAndFolderPath(String projectId, FolderTypeEnum folderType, String folderPath);
}
