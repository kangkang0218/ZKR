package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectGitRepositoryTestResponse {
    private boolean success;
    private String message;
    private String repository;
    private String defaultBranch;
}
