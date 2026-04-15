package com.smartlab.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectChatMessageRequest {
    @NotBlank(message = "消息内容不能为空")
    private String content;

    private String stageTag;
}
