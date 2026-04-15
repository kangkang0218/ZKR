package com.smartlab.erp.finance.service;

import com.smartlab.erp.entity.FlowType;
import com.smartlab.erp.entity.ProjectStatus;
import com.smartlab.erp.entity.SysProject;
import com.smartlab.erp.entity.SysProjectMember;
import com.smartlab.erp.entity.User;
import com.smartlab.erp.finance.dto.FinanceCostBatchRunResponse;
import com.smartlab.erp.finance.dto.FinanceCostPreviewItem;
import com.smartlab.erp.finance.dto.FinanceCostPreviewResponse;
import com.smartlab.erp.finance.dto.FinanceVentureRef;
import com.smartlab.erp.finance.entity.FinanceCostBatch;
import com.smartlab.erp.finance.entity.FinanceCostEntry;
import com.smartlab.erp.finance.entity.FinanceCostSummary;
import com.smartlab.erp.finance.entity.FinanceVentureProfile;
import com.smartlab.erp.finance.enums.FinanceBatchStatus;
import com.smartlab.erp.finance.repository.FinanceCostBatchRepository;
import com.smartlab.erp.finance.repository.FinanceCostEntryRepository;
import com.smartlab.erp.finance.repository.FinanceCostSummaryRepository;
import com.smartlab.erp.finance.repository.FinanceVentureProfileRepository;
import com.smartlab.erp.finance.support.FinanceAmounts;
import com.smartlab.erp.repository.SysProjectMemberRepository;
import com.smartlab.erp.repository.SysProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FinanceCostBatchService {

    private static final Pattern LEDGER_MONTH_PATTERN = Pattern.compile("\\d{4}-\\d{2}");
    private static final BigDecimal STANDARD_HOURS = new BigDecimal("160.00");
    private static final BigDecimal LEVERAGE_MULTIPLIER = new BigDecimal("2.0");

    private final FinanceCostBatchRepository costBatchRepository;
    private final FinanceCostEntryRepository costEntryRepository;
    private final FinanceCostSummaryRepository costSummaryRepository;
    private final FinanceVentureProfileRepository ventureProfileRepository;
    private final SysProjectMemberRepository projectMemberRepository;
    private final SysProjectRepository projectRepository;

    @Transactional
    public FinanceCostBatchRunResponse runBatch(String ledgerMonth) {
        return runBatch(ledgerMonth, false);
    }

    @Transactional
    public FinanceCostBatchRunResponse runBatch(String ledgerMonth, boolean rerunExistingMonth) {
        validateLedgerMonth(ledgerMonth);

        FinanceCostBatch existingBatch = costBatchRepository.findTopByLedgerMonthOrderByIdDesc(ledgerMonth)
                .orElse(null);
        if (existingBatch != null && existingBatch.getStatus() == FinanceBatchStatus.RUNNING) {
            throw new IllegalArgumentException("cost batch is already running for ledger month " + ledgerMonth);
        }

        if (!rerunExistingMonth) {
            if (existingBatch != null) {
                return buildRunResponse(existingBatch, true);
            }
        }

        FinanceCostBatch batch = costBatchRepository.save(FinanceCostBatch.builder()
                .ledgerMonth(ledgerMonth)
                .status(FinanceBatchStatus.RUNNING)
                .batchDate(LocalDate.now())
                .startedAt(Instant.now())
                .build());

        List<SysProject> projects = loadActiveProjectFlowProjects();
        try {
            int generatedRecordCount = 0;
            BigDecimal totalSettlementCost = BigDecimal.ZERO;

            for (SysProject project : projects) {
                costEntryRepository.deleteByProject_ProjectIdAndLedgerMonth(project.getProjectId(), ledgerMonth);

                List<MemberAllocation> allocations = buildAllocations(project);
                List<FinanceCostEntry> entries = new ArrayList<>();
                BigDecimal totalLaborCost = BigDecimal.ZERO;
                BigDecimal totalProjectSettlementCost = BigDecimal.ZERO;
                Long sourceId = resolveSourceId(project);

                for (MemberAllocation allocation : allocations) {
                    BigDecimal laborCost = allocation.allocatedBaseCost();
                    BigDecimal finalSettlementCost = FinanceAmounts.scale(laborCost.multiply(LEVERAGE_MULTIPLIER));
                    totalLaborCost = FinanceAmounts.add(totalLaborCost, laborCost);
                    totalProjectSettlementCost = FinanceAmounts.add(totalProjectSettlementCost, finalSettlementCost);
                    entries.add(FinanceCostEntry.builder()
                            .batch(batch)
                            .project(project)
                            .user(allocation.user())
                            .ledgerMonth(ledgerMonth)
                            .workHours(STANDARD_HOURS)
                            .laborCost(laborCost)
                            .middlewareRoyaltyFee(BigDecimal.ZERO)
                            .finalSettlementCost(finalSettlementCost)
                            .sourceTable("sys_project")
                            .sourceId(sourceId)
                            .build());
                }

                costEntryRepository.saveAll(entries);

                FinanceCostSummary summary = costSummaryRepository.findByProject_ProjectIdAndLedgerMonth(project.getProjectId(), ledgerMonth)
                        .orElseGet(FinanceCostSummary::new);
                summary.setBatch(batch);
                summary.setProject(project);
                summary.setLedgerMonth(ledgerMonth);
                summary.setTotalLaborCost(FinanceAmounts.scale(totalLaborCost));
                summary.setTotalMiddlewareFee(BigDecimal.ZERO.setScale(2));
                summary.setTotalSettlementCost(FinanceAmounts.scale(totalProjectSettlementCost));
                summary.setEntryCount(entries.size());
                costSummaryRepository.save(summary);

                generatedRecordCount += entries.size();
                totalSettlementCost = FinanceAmounts.add(totalSettlementCost, totalProjectSettlementCost);
            }

            batch.setGeneratedRecordCount(generatedRecordCount);
            batch.setCompletedAt(Instant.now());
            batch.setStatus(FinanceBatchStatus.COMPLETED);
            costBatchRepository.save(batch);

            return FinanceCostBatchRunResponse.builder()
                    .batchId(batch.getId())
                    .ledgerMonth(batch.getLedgerMonth())
                    .status(batch.getStatus())
                    .ventureCount(projects.size())
                    .generatedRecordCount(generatedRecordCount)
                    .totalSettlementCost(FinanceAmounts.scale(totalSettlementCost))
                    .reusedExistingBatch(false)
                    .build();
        } catch (RuntimeException ex) {
            batch.setCompletedAt(Instant.now());
            batch.setStatus(FinanceBatchStatus.FAILED);
            batch.setRemark(ex.getMessage());
            costBatchRepository.save(batch);
            throw ex;
        }
    }

    private FinanceCostBatchRunResponse buildRunResponse(FinanceCostBatch batch, boolean reusedExistingBatch) {
        List<FinanceCostSummary> summaries = costSummaryRepository.findByLedgerMonth(batch.getLedgerMonth());
        BigDecimal totalSettlementCost = summaries.stream()
                .map(FinanceCostSummary::getTotalSettlementCost)
                .reduce(BigDecimal.ZERO, FinanceAmounts::add);
        int ventureCount = loadActiveProjectFlowProjects().size();

        return FinanceCostBatchRunResponse.builder()
                .batchId(batch.getId())
                .ledgerMonth(batch.getLedgerMonth())
                .status(batch.getStatus())
                .ventureCount(ventureCount)
                .generatedRecordCount(batch.getGeneratedRecordCount())
                .totalSettlementCost(FinanceAmounts.scale(totalSettlementCost))
                .reusedExistingBatch(reusedExistingBatch)
                .build();
    }

    private List<SysProject> loadActiveProjectFlowProjects() {
        return projectRepository.findByFlowTypeAndProjectStatusNotOrderByUpdatedAtDescCreatedAtDesc(FlowType.PROJECT, ProjectStatus.COMPLETED);
    }

    private Long resolveSourceId(SysProject project) {
        return ventureProfileRepository.findByProject_ProjectId(project.getProjectId())
                .map(FinanceVentureProfile::getLegacyVentureId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public FinanceCostPreviewResponse preview(Long ventureId, String ledgerMonth) {
        validateLedgerMonth(ledgerMonth);
        FinanceVentureProfile venture = ventureProfileRepository.findByLegacyVentureId(ventureId)
                .orElseThrow(() -> new IllegalArgumentException("Finance venture not found: " + ventureId));

        List<FinanceCostEntry> entries = costEntryRepository.findByProject_ProjectIdAndLedgerMonth(
                venture.getProject().getProjectId(), ledgerMonth);

        List<FinanceCostPreviewItem> items = entries.stream()
                .sorted(Comparator.comparing(entry -> entry.getUser() == null ? "" : entry.getUser().getUserId()))
                .map(entry -> FinanceCostPreviewItem.builder()
                        .userId(entry.getUser() == null ? null : entry.getUser().getUserId())
                        .userName(entry.getUser() == null ? null : entry.getUser().getName())
                        .role(entry.getUser() == null ? null : entry.getUser().getRole())
                        .workHours(FinanceAmounts.scale(entry.getWorkHours()))
                        .laborCost(FinanceAmounts.scale(entry.getLaborCost()))
                        .finalSettlementCost(FinanceAmounts.scale(entry.getFinalSettlementCost()))
                        .build())
                .toList();

        BigDecimal totalSettlementCost = entries.stream()
                .map(FinanceCostEntry::getFinalSettlementCost)
                .reduce(BigDecimal.ZERO, FinanceAmounts::add);
        FinanceCostBatch batch = costBatchRepository.findTopByLedgerMonthOrderByIdDesc(ledgerMonth).orElse(null);

        return FinanceCostPreviewResponse.builder()
                .venture(toVentureRef(venture))
                .ledgerMonth(ledgerMonth)
                .batchId(batch == null ? null : batch.getId())
                .batchStatus(batch == null ? null : batch.getStatus())
                .entryCount(items.size())
                .totalSettlementCost(FinanceAmounts.scale(totalSettlementCost))
                .items(items)
                .build();
    }

    private List<MemberAllocation> buildAllocations(SysProject project) {
        List<SysProjectMember> members = projectMemberRepository.findByProjectId(project.getProjectId());
        if (members.isEmpty()) {
            return List.of(new MemberAllocation(project.getManager(), FinanceAmounts.scale(project.getCost())));
        }

        BigDecimal totalCost = FinanceAmounts.scale(project.getCost());
        int totalWeight = members.stream()
                .mapToInt(member -> member.getWeight() != null && member.getWeight() > 0 ? member.getWeight() : 1)
                .sum();

        List<MemberAllocation> allocations = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < members.size(); i++) {
            SysProjectMember member = members.get(i);
            BigDecimal amount;
            if (i == members.size() - 1) {
                amount = FinanceAmounts.scale(totalCost.subtract(allocated));
            } else {
                int weight = member.getWeight() != null && member.getWeight() > 0 ? member.getWeight() : 1;
                amount = FinanceAmounts.scale(totalCost.multiply(BigDecimal.valueOf(weight))
                        .divide(BigDecimal.valueOf(totalWeight), 2, java.math.RoundingMode.HALF_UP));
                allocated = FinanceAmounts.add(allocated, amount);
            }
            allocations.add(new MemberAllocation(member.getUser(), amount));
        }
        return allocations;
    }

    private void validateLedgerMonth(String ledgerMonth) {
        if (ledgerMonth == null || !LEDGER_MONTH_PATTERN.matcher(ledgerMonth).matches()) {
            throw new IllegalArgumentException("ledgerMonth must match YYYY-MM");
        }
    }

    private FinanceVentureRef toVentureRef(FinanceVentureProfile venture) {
        return FinanceVentureRef.builder()
                .projectId(venture.getProject().getProjectId())
                .legacyVentureId(venture.getLegacyVentureId())
                .displayName(venture.getDisplayName())
                .legacyStage(venture.getLegacyStage())
                .build();
    }

    private record MemberAllocation(User user, BigDecimal allocatedBaseCost) {
    }
}
