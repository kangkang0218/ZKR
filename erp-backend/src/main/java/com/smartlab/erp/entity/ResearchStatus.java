package com.smartlab.erp.entity;

/**
 * 科研流（Research Flow）专用状态枚举
 * 生命周期：发起与初探 → 大群深化 → 实施前定调 → 施工执行 → 评测入库为中间件
 */
public enum ResearchStatus {
    INIT,                   // 发起
    BLUEPRINT,              // 小群蓝图
    EXPANSION,              // 大群深化
    DESIGN,                 // 实施前设计
    EXECUTION,              // 施工执行
    EVALUATION,             // 评测
    ARCHIVE,                // 入库完成
    SHELVED,                // 已搁置

    // 兼容旧状态（历史数据）
    PROBE,
    DEEPENING,
    PRE_EXECUTION,
    CONSTRUCTION,
    ARCHIVED_TO_MIDDLEWARE
}
