package com.smartlab.erp.finance.schedule;

import com.smartlab.erp.finance.service.FinanceCostBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceCostBatchScheduler {

    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter LEDGER_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final FinanceCostBatchService financeCostBatchService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Shanghai")
    public void runNightlyProjectLaborCostBatch() {
        String ledgerMonth = ZonedDateTime.now(SHANGHAI_ZONE).format(LEDGER_MONTH_FORMATTER);
        try {
            var result = financeCostBatchService.runBatch(ledgerMonth, true);
            log.info("Nightly project labor cost batch completed for {} with batchId={} and generatedRecordCount={}",
                    ledgerMonth,
                    result.getBatchId(),
                    result.getGeneratedRecordCount());
        } catch (RuntimeException ex) {
            log.error("Nightly project labor cost batch failed for {}", ledgerMonth, ex);
        }
    }
}
