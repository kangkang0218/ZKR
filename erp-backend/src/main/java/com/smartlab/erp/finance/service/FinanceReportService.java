package com.smartlab.erp.finance.service;

import com.smartlab.erp.finance.dto.ReportDataRow;
import com.smartlab.erp.finance.dto.ReportGenerateRequest;
import com.smartlab.erp.finance.dto.ReportGenerateResponse;
import com.smartlab.erp.finance.dto.ReportPromptDto;
import com.smartlab.erp.finance.dto.ReportSpec;
import com.smartlab.erp.finance.entity.FinanceReportPrompt;
import com.smartlab.erp.finance.repository.FinanceReportPromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceReportService {

    private final ReportSpecParserService specParserService;
    private final ReportDataService reportDataService;
    private final FinanceReportPromptRepository promptRepository;

    @Transactional
    public ReportGenerateResponse generate(ReportGenerateRequest request, String creatorUserId, String creatorName) {
        String text = request.getText() == null ? "" : request.getText().trim();
        if (text.isBlank()) {
            throw new IllegalArgumentException("请输入报表描述");
        }
        ReportSpec spec = specParserService.parse(text);
        List<ReportDataRow> rows = reportDataService.aggregate(spec);

        Long savedPromptId = null;
        if (Boolean.TRUE.equals(request.getSave())) {
            savedPromptId = savePrompt(text, creatorUserId, creatorName);
        }

        return ReportGenerateResponse.builder()
                .spec(spec)
                .rows(rows)
                .total(rows.size())
                .generatedAt(Instant.now())
                .provider("LLM")
                .savedPromptId(savedPromptId)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReportPromptDto> listPrompts() {
        return promptRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePrompt(Long id) {
        if (!promptRepository.existsById(id)) {
            throw new IllegalArgumentException("保存的报表描述不存在（id=" + id + "）");
        }
        promptRepository.deleteById(id);
    }

    private Long savePrompt(String text, String creatorUserId, String creatorName) {
        FinanceReportPrompt prompt = FinanceReportPrompt.builder()
                .promptText(text)
                .creatorUserId(creatorUserId)
                .creatorName(creatorName)
                .build();
        return promptRepository.save(prompt).getId();
    }

    private ReportPromptDto toDto(FinanceReportPrompt prompt) {
        return ReportPromptDto.builder()
                .id(prompt.getId())
                .promptText(prompt.getPromptText())
                .creatorUserId(prompt.getCreatorUserId())
                .creatorName(prompt.getCreatorName())
                .createdAt(prompt.getCreatedAt())
                .build();
    }
}
