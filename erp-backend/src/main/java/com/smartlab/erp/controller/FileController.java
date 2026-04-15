package com.smartlab.erp.controller;

import com.smartlab.erp.dto.FileUploadResponseDTO;
import com.smartlab.erp.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        FileUploadResponseDTO response = fileService.uploadFile(file);
        return ResponseEntity.ok(response);
    }
}
