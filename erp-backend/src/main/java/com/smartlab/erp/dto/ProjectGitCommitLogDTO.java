package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectGitCommitLogDTO {
    private String sha;
    private String authorName;
    private String authorEmail;
    private String pushedAt;
    private String message;
    private String commitUrl;
}
