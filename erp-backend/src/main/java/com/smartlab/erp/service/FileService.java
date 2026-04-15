package com.smartlab.erp.service;

import com.smartlab.erp.dto.FileUploadResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir:/uploads}") // 从配置文件读取上传目录，默认为 /uploads
    private String uploadDir;

    public FileUploadResponseDTO uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // 使用 UUID 生成唯一文件名，防止文件覆盖
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // 构建目标文件路径
        Path targetPath = Paths.get(uploadDir, "temp", uniqueFilename);
        File directory = targetPath.toFile().getParentFile();

        if (!directory.exists()) {
            directory.mkdirs();
        }

        try {
            file.transferTo(targetPath);
            // 返回文件的相对路径或可访问的 URL
            String fileUrl = "/files/temp/" + uniqueFilename; // 假设可以通过 /files/temp/ 访问
            return FileUploadResponseDTO.builder()
                    .fileName(originalFilename)
                    .filePath(fileUrl)
                    .fileType(Objects.requireNonNull(file.getContentType()))
                    .fileSize(file.getSize())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }
}
