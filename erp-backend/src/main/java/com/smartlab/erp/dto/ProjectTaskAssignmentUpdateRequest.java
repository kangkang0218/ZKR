package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskAssignmentUpdateRequest {
    private List<AssignmentItem> assignments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentItem {
        private String userId;
        private String taskName;
        private String expectedOutput;
        private Instant expectedEndDate;
    }
}
