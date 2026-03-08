package com.governance.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务启动入口。
 *
 * <p>该服务是整个微服务体系的统一流量入口，负责请求转发、JWT 校验、
 * 请求上下文透传以及 Swagger 聚合等能力。</p>
 */
@SpringBootApplication
public class GovernanceGatewayApplication {

    /**
     * 启动网关服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GovernanceGatewayApplication.class, args);
    }
}
