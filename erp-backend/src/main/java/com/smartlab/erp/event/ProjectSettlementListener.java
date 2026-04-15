package com.smartlab.erp.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 项目结算归档事件监听器
 * 
 * 监听 ProjectSettlementEvent 事件，异步执行归档操作。
 * 当前为 Mock 实现，预留了 Zookeeper 分布式协调接入点。
 */
@Slf4j
@Component
public class ProjectSettlementListener {

    /**
     * 异步处理项目结算归档
     * 
     * 触发时机：Manager 完成 OCR 凭证校验后，项目状态跃迁至 SETTLEMENT。
     * 
     * @param event 结算归档事件
     */
    @Async
    @EventListener
    public void handleSettlement(ProjectSettlementEvent event) {
        log.info("========== [归档事件] 开始处理项目结算归档 ==========");
        log.info("[归档事件] 项目ID: {}", event.getProjectId());
        log.info("[归档事件] 操作人ID: {}", event.getOperatorUserId());
        log.info("[归档事件] 凭证路径: {}", event.getVoucherFilePath());

        // ===== Step 1: 文件归档 =====
        // TODO: 引入 Zookeeper 协调分布式文件归档
        //  ZookeeperClient zkClient = new ZookeeperClient("zk://localhost:2181");
        //  DistributedLock lock = zkClient.acquireLock("/erp/archive/" + event.getProjectId());
        //  try {
        //      // 1. 锁定项目文件目录，防止并发归档
        //      // 2. 将 Manager 归档区和工程师作业区的文件迁移至冷存储
        //      // 3. 生成归档索引文件
        //      // 4. 更新项目的归档状态为 ARCHIVED
        //  } finally {
        //      lock.release();
        //  }

        // ===== Step 2: 财务结算通知 =====
        // TODO: 发送消息到 MQ (如 RocketMQ/Kafka)，通知财务系统进行结算
        //  messageProducer.send("topic-finance-settlement", event.getProjectId());

        // ===== Step 3: 归档日志记录 =====
        // TODO: 写入归档审计日志
        //  auditLogService.log(event.getProjectId(), "SETTLEMENT_COMPLETE", event.getOperatorUserId());

        log.info("[归档事件] 项目 {} 归档处理完成 (Mock)", event.getProjectId());
        log.info("========== [归档事件] 处理结束 ==========");
    }
}
