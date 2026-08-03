package com.smartlab.erp.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportPromptDto {

    private Long id;
    private String promptText;
    private String creatorUserId;
    private String creatorName;
    private Instant createdAt;
}
