package com.smartlab.erp.command.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandExecuteResponse {
    private String actionType;
    private String resultSummary;
    private String navigateTo;
    private Map<String, Object> result;
}
