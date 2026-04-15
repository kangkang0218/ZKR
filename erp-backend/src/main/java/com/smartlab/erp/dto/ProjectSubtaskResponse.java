package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSubtaskResponse {
    private Long id;
    private String title;
    private String description;
    private String assigneeUserId;
    private String assigneeName;
    private Integer sortOrder;
    private boolean completed;
    private String completedAt;
}
