package com.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 数据源服务启动入口。
 *
 * <p>该服务负责维护数据源主数据，并对外提供统计概览，
 * 同时为元数据服务提供内部查询能力。</p>
 */
@SpringBootApplication
@EnableFeignClients
public class DataSourceApplication {

    /**
     * 启动数据源服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DataSourceApplication.class, args);
    }
}
