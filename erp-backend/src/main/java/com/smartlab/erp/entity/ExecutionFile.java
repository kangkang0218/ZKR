package com.smartlab.erp.entity;

import com.smartlab.erp.enums.FolderTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * [扩展表 - Sidecar 模式] 实施阶段双盲隔离文件表
 * 
 * 实现"强制双盲文件隔离"：
 * - folderType = A_MANAGER_ARCHIVE → Manager 专属归档区
 * - folderType = B_ENGINEER_WORK   → 工程师独立作业区
 * 
 * 查询时按 folderType 分区，Manager 可统筹查看 A/B 区，工程师仅可见自己上传到 B 区的文件。
 * 通过 projectId 物理隔离，绝不影响其他项目。
 */
@Entity
@Table(name = "execution_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联项目ID (物理隔离键) */
    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    /** 文件隔离区类型 */
    @Enumerated(EnumType.STRING)
    @Column(name = "folder_type", nullable = false, length = 30)
    private FolderTypeEnum folderType;

    /** 上传人用户ID (双盲追踪的核心字段) */
    @Column(name = "uploader_user_id", nullable = false, length = 64)
    private String uploaderUserId;

    /** 上传人姓名 (冗余存储，方便展示) */
    @Column(name = "uploader_name", length = 100)
    private String uploaderName;

    /** 原始文件名 */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /** 管理归档目录路径，根目录为空 */
    @Column(name = "secondary_category", length = 255)
    private String secondaryCategory;

    /** 文件后缀类型 (如 PDF, DOCX) */
    @Column(name = "file_type", length = 20)
    private String fileType;

    /** 物理磁盘存储路径 */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    /** 文件大小 (字节) */
    @Column(name = "file_size")
    private Long fileSize;

    /** 上传时间 */
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uploadedAt == null) {
            this.uploadedAt = Instant.now();
        }
    }
}
