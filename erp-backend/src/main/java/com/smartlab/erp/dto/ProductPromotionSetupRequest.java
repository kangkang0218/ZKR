package com.smartlab.erp.dto;

import com.smartlab.erp.entity.ProjectType;
import com.smartlab.erp.enums.ProjectTierEnum;
import lombok.Data;

import java.util.List;

/**
 * 阶段 2 - 推广与 Demo 组队请求体
 */
@Data
public class ProductPromotionSetupRequest {

    /**
     * 推广负责 IC 用户 ID
     */
    private String promotionIcUserId;

    /**
     * 其他推广成员用户 ID 列表（不能为空）
     */
    private List<String> promotionMemberIds;

    /**
     * Demo 工程师用户 ID 列表（要求 4 名）
     */
    private List<String> demoEngineerIds;

    /**
     * 推广阶段强制评级
     */
    private ProjectTierEnum projectTier;

    /**
     * 推广阶段强制行业分类
     */
    private ProjectType projectType;

    private String demoEngineeringOwnerUserId;

    private String demoFileOwnerUserId;

    private String demoDescriptionOwnerUserId;

    private String demoFeasibilityOwnerUserId;
}
