package com.smartlab.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectSubtaskRequest {
    @NotBlank
    private String title;
    private String description;
    private String assigneeUserId;
    private Integer sortOrder;
}
