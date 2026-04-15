package com.smartlab.erp.entity;

import com.smartlab.erp.enums.FolderTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "execution_archive_folder")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionArchiveFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "folder_type", nullable = false, length = 30)
    private FolderTypeEnum folderType;

    @Column(name = "folder_path", nullable = false, length = 255)
    private String folderPath;

    @Column(name = "parent_path", length = 255)
    private String parentPath;

    @Column(name = "created_by_user_id", length = 64)
    private String createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
