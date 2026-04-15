package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalMessageResponse {
    private Long id;
    private String messageType;
    private String title;
    private String content;
    private String projectId;
    private boolean read;
    private String createdAt;
}
