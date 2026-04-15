package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectChatMessageResponse {
    private Long id;
    private String projectId;
    private String senderUserId;
    private String senderName;
    private String content;
    private String stageTag;
    private String createdAt;
}
