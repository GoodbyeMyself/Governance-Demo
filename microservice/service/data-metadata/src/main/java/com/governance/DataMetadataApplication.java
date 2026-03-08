package com.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 元数据服务启动入口。
 *
 * <p>该服务负责元数据采集任务管理、任务统计以及工作台聚合数据输出，
 * 并依赖 Feign 调用数据源服务获取辅助信息。</p>
 */
@SpringBootApplication
@EnableFeignClients
public class DataMetadataApplication {

    /**
     * 启动元数据服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DataMetadataApplication.class, args);
    }
}
