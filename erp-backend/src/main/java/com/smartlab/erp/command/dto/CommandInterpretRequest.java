package com.smartlab.erp.command.dto;

import lombok.Data;

@Data
public class CommandInterpretRequest {
    private String text;
    private String currentRoute;
    private String currentProjectId;
}
