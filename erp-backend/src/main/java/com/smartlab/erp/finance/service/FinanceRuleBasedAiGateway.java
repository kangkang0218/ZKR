package com.smartlab.erp.finance.service;

import com.smartlab.erp.finance.dto.FinanceAiContextBlock;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class FinanceRuleBasedAiGateway implements FinanceAiGateway {

    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^a-z0-9]+");

    @Override
    public String generateAnswer(String message, List<FinanceAiContextBlock> contextBlocks) {
        String normalizedMessage = normalize(message);
        List<FinanceAiContextBlock> relevant = contextBlocks.stream()
                .filter(Objects::nonNull)
                .filter(block -> score(normalizedMessage, block) > 0)
                .limit(3)
                .toList();

        if (relevant.isEmpty()) {
            relevant = contextBlocks.stream().filter(Objects::nonNull).limit(2).toList();
        }

        String details = relevant.stream()
                .map(block -> block.getTitle() + ": " + block.getContent())
                .reduce((left, right) -> left + " | " + right)
                .orElse("No finance snapshot is currently available.");

        return "Finance assistant answer for '" + message.trim() + "': " + details;
    }

    private int score(String normalizedMessage, FinanceAiContextBlock block) {
        String haystack = normalize(block.getTitle()) + " " + normalize(block.getContent()) + " " + normalize(block.getSourceType());
        int score = 0;
        for (String token : TOKEN_SPLIT.split(normalizedMessage)) {
            if (!token.isBlank() && haystack.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
