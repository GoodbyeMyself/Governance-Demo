package com.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * IoT 采集服务启动入口。
 *
 * <p>该服务负责维护设备采集任务，并依赖 IoT 设备服务获取主数据。</p>
 */
@SpringBootApplication
@EnableFeignClients
public class IotCollectionApplication {

    /**
     * 启动 IoT 采集服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(IotCollectionApplication.class, args);
    }
}
