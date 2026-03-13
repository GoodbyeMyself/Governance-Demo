package com.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * IoT 设备服务启动入口。
 *
 * <p>该服务负责维护物联网设备主数据，并为采集服务提供内部查询能力。</p>
 */
@SpringBootApplication
@EnableFeignClients
public class IotDeviceApplication {

    /**
     * 启动 IoT 设备服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(IotDeviceApplication.class, args);
    }
}
