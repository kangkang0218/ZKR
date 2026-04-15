package com.smartlab.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectChatParticipantResponse {
    private String userId;
    private String name;
    private String role;
    private String avatar;
    private Boolean hiddenAvatar;
}
