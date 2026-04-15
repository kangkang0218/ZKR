package com.smartlab.erp.dto;

import lombok.Data;

@Data
public class ProjectGitRepositoryCreateRequest {
    private String repositoryUrl;
    private String accessToken;
    private String branch;
    private String provider;
}
