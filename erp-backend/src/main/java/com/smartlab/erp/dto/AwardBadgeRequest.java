package com.smartlab.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AwardBadgeRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String badgeName;
    private String badgeIcon;
    private String badgeColor;
    private Boolean hiddenAvatar;
}
