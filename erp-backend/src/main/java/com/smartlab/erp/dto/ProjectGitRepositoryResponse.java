package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectGitRepositoryResponse {
    private Long id;
    private String repositoryUrl;
    private String branch;
    private String provider;
    private String createdBy;
    private String createdAt;
    private String lastTestStatus;
    private String lastTestMessage;
    private String lastTestedAt;
    private boolean tokenConfigured;
}
