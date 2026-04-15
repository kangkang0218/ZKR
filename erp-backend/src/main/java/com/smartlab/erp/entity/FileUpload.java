package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 文件上传表
 */
@Entity
@Table(name = "file_upload")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upload_id")
    private Long uploadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow_type", nullable = false, length = 20)
    private FlowType flowType;

    @Column(name = "flow_id", nullable = false)
    private Long flowId;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(length = 100)
    private String mimeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploader;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(length = 20)
    private String fileType;

    @PrePersist
    protected void onCreate() {
        if (this.uploadedAt == null) {
            this.uploadedAt = Instant.now();
        }
    }
}
