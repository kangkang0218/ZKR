package com.smartlab.erp.finance.service;

import com.smartlab.erp.finance.dto.FinanceAiContextBlock;

import java.util.List;

public interface FinanceAiGateway {
    String generateAnswer(String message, List<FinanceAiContextBlock> contextBlocks);
}
