package com.smartlab.erp.entity;

/**
 * 产品流状态枚举（轨道 B）
 * 生命周期：创意孵化 → 推广组队 → Demo 实施 → 虚拟会议决策 → 测试与上线 → 转化/搁置
 */
public enum ProductStatus {

    IDEA,             // 创意孵化
    PROMOTION,        // 推广组队阶段
    DEMO_EXECUTION,   // Demo 实施阶段
    MEETING_DECISION, // 虚拟会议决策阶段
    TESTING,          // 测试与上线阶段
    LAUNCHED,         // 已转化为正式实施项目
    SHELVED           // 搁置 / 流产
}
