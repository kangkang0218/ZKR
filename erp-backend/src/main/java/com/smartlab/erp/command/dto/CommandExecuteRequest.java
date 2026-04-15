package com.smartlab.erp.command.dto;

import java.util.Map;
import lombok.Data;

@Data
public class CommandExecuteRequest {
    private String actionType;
    private Map<String, Object> payload;
}
