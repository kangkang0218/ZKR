package com.smartlab.erp.entity;

/**
 * 三流并行架构 - 流程类型枚举
 *
 * 系统处理三类平行的业务交付，各自独立核算，但逻辑互通：
 * - PROJECT: 项目流 (Project Flow) - 对外接单，交付物为非软件（咨询报告、硬件集成）
 * - PRODUCT: 产品流 (Product Flow) - 内部创投，交付物为软件系统、算法模型
 * - RESEARCH: 科研流 (Research Flow) - 技术攻坚，最终入库为中间件资产
 */
public enum FlowType {
    /**
     * 轨道 A：项目流
     * 生命周期：发起 -> 组队 -> 实施 -> 结算
     */
    PROJECT,

    /**
     * 轨道 B：产品流
     * 生命周期：创意孵化 -> 推广组队 -> Demo实施 -> 虚拟会议决策 -> 测试与上线 -> 转化/搁置
     */
    PRODUCT,

    /**
     * 轨道 C：科研流
     * 生命周期：初探 -> 大群深化 -> 实施前定调 -> 施工执行 -> 评测入库为中间件
     */
    RESEARCH
}
