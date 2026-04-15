package com.smartlab.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * [Smart Lab] 项目资产/文件实体
 * 记录上传的文件信息，用于文件回显和生命周期触发
 */
@Entity
@Table(name = "project_asset")
@Data // 包含 Getter, Setter, ToString 等
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联到项目 (多对一)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private SysProject project;

    @Column(nullable = false)
    private String fileName; // 原始文件名 (e.g., "需求文档_v1.docx")

    @Column(nullable = false)
    private String fileType; // 文件后缀类型 (e.g., "DOCX")

    @Column(nullable = false)
    private String filePath; // 物理磁盘路径

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "file_data", columnDefinition = "bytea")
    private byte[] fileData;

    @Column(name = "content_type", length = 255)
    private String contentType;

    private Long fileSize;   // 文件大小 (字节)

    private String uploaderName; // 上传者姓名 (冗余存储，方便展示)

    @Column(nullable = false)
    private Instant uploadedAt; // 上传时间

    /**
     * 可选：资产分类，例如 Demo 网关中的 ENGINEERING / DEMO_FILE / DESCRIPTION / FEASIBILITY
     */
    @Column(name = "asset_category", length = 50)
    private String assetCategory;
}
