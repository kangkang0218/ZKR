package com.smartlab.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync // 启用异步支持，用于 ProjectSettlementListener 的异步归档处理
@EnableScheduling
public class ErpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpBackendApplication.class, args);
    }

}
