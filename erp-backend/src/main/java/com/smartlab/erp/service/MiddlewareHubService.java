package com.smartlab.erp.service;

import com.smartlab.erp.dto.MiddlewareAssetUpsertRequest;
import com.smartlab.erp.dto.MiddlewareRepositoryItemDTO;
import com.smartlab.erp.entity.FlowType;
import com.smartlab.erp.entity.MiddlewareAsset;
import com.smartlab.erp.entity.User;
import com.smartlab.erp.exception.BusinessException;
import com.smartlab.erp.finance.repository.FinanceMiddlewareUsageRepository;
import com.smartlab.erp.repository.MiddlewareAssetRepository;
import com.smartlab.erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MiddlewareHubService {

    private final MiddlewareAssetRepository middlewareAssetRepository;
    private final FinanceMiddlewareUsageRepository financeMiddlewareUsageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "middlewareHub:list", key = "{#keyword, #flowType}")
    public List<MiddlewareAsset> list(String keyword, FlowType flowType) {
        if (flowType != null) {
            return middlewareAssetRepository.findBySourceFlowTypeOrderByCreatedAtDesc(flowType);
        }
        if (keyword != null && !keyword.isBlank()) {
            return middlewareAssetRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(keyword.trim());
        }
        return middlewareAssetRepository.findAll().stream().sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "middlewareHub:get", key = "#id")
    public MiddlewareAsset get(Long id) {
        return middlewareAssetRepository.findById(id)
                .orElseThrow(() -> new BusinessException("中间件资产不存在: " + id));
    }

    @Transactional(readOnly = true)
    public List<MiddlewareRepositoryItemDTO> repositoryView() {
        List<MiddlewareAsset> assets = middlewareAssetRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        Set<Long> middlewareIds = assets.stream().map(MiddlewareAsset::getId).collect(Collectors.toSet());
        Map<Long, Long> invokeCountMap = financeMiddlewareUsageRepository.findByMiddleware_IdIn(middlewareIds).stream()
                .collect(Collectors.groupingBy(usage -> usage.getMiddleware().getId(), Collectors.counting()));

        Map<String, String> creatorNameMap = assets.stream()
                .map(MiddlewareAsset::getOwnerUserId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toMap(id -> id, id -> userRepository.findById(id)
                        .map(u -> u.getName() == null || u.getName().isBlank() ? u.getUsername() : u.getName())
                        .orElse(id)));

        return assets.stream().map(asset -> MiddlewareRepositoryItemDTO.builder()
                .middlewareId(asset.getId())
                .middlewareName(asset.getName())
                .createdAt(asset.getCreatedAt() == null ? null : asset.getCreatedAt().toString())
                .creator(creatorNameMap.getOrDefault(asset.getOwnerUserId(), "未知"))
                .invokeCount(invokeCountMap.getOrDefault(asset.getId(), 0L))
                .callPrice(asset.getUnitPrice())
                .currency(asset.getCurrency() == null || asset.getCurrency().isBlank() ? "CNY" : asset.getCurrency())
                .build()).toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "middlewareHub:list", allEntries = true),
            @CacheEvict(cacheNames = "middlewareHub:get", allEntries = true)
    })
    public MiddlewareAsset create(MiddlewareAssetUpsertRequest request) {
        MiddlewareAsset asset = new MiddlewareAsset();
        applyRequest(asset, request);
        return middlewareAssetRepository.save(asset);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "middlewareHub:list", allEntries = true),
            @CacheEvict(cacheNames = "middlewareHub:get", allEntries = true)
    })
    public MiddlewareAsset update(Long id, MiddlewareAssetUpsertRequest request) {
        MiddlewareAsset asset = get(id);
        applyRequest(asset, request);
        return middlewareAssetRepository.save(asset);
    }

    private void applyRequest(MiddlewareAsset asset, MiddlewareAssetUpsertRequest request) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException("中间件名称不能为空");
        }
        if (request.getSourceProjectId() == null || request.getSourceProjectId().isBlank()) {
            throw new BusinessException("来源项目ID不能为空");
        }
        asset.setName(request.getName().trim());
        asset.setDescription(request.getDescription());
        asset.setSourceProjectId(request.getSourceProjectId().trim());
        asset.setSourceFlowType(request.getSourceFlowType());
        asset.setSourceStatus(request.getSourceStatus());
        asset.setOwnerUserId(request.getOwnerUserId());
        asset.setRepoUrl(request.getRepoUrl());
        asset.setRating(request.getRating());
        asset.setPricingModel(request.getPricingModel());
        asset.setUnitPrice(request.getUnitPrice());
        asset.setInternalCostPrice(request.getInternalCostPrice());
        asset.setMarketReferencePrice(request.getMarketReferencePrice());
        asset.setCurrency(request.getCurrency());
        asset.setBillingUnit(request.getBillingUnit());
        asset.setVersionTag(request.getVersionTag());
        asset.setLifecycleStatus(request.getLifecycleStatus());
        asset.setExtraMetadata(request.getExtraMetadata());
    }
}
