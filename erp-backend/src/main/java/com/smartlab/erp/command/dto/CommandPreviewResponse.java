package com.smartlab.erp.command.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandPreviewResponse {
    private String actionType;
    private String title;
    private String summary;
    private boolean confirmRequired;
    private boolean canExecute;
    private List<String> previewLines;
    private List<String> missingFields;
    private List<String> warnings;
    private Map<String, Object> payload;
}
