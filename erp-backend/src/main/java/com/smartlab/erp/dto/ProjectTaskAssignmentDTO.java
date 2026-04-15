package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskAssignmentDTO {
    private String userId;
    private String name;
    private String role;
    private String taskName;
    private String expectedOutput;
    private Instant expectedEndDate;
}
