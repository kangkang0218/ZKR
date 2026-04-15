package com.smartlab.erp.dto;

import com.smartlab.erp.entity.FlowType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MiddlewareAssetUpsertRequest {
    private String name;
    private String description;
    private String sourceProjectId;
    private FlowType sourceFlowType;
    private String sourceStatus;
    private String ownerUserId;
    private String repoUrl;
    private String rating;
    private String pricingModel;
    private BigDecimal unitPrice;
    private BigDecimal internalCostPrice;
    private BigDecimal marketReferencePrice;
    private String currency;
    private String billingUnit;
    private String versionTag;
    private String lifecycleStatus;
    private String extraMetadata;
}
