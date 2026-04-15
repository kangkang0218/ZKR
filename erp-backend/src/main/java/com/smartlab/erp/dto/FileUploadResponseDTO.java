package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponseDTO {
    private String fileName;
    private String filePath; // This will be the URL or path to access the file
    private String fileType;
    private Long fileSize;
}
