package com.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证中心启动入口。
 *
 * <p>认证中心主要负责注册、登录、当前用户查询与退出登录，
 * 并通过 Feign 调用基础管理服务获取用户主数据。</p>
 */
@SpringBootApplication
@EnableFeignClients
public class GovernanceAuthServiceApplication {

    /**
     * 启动认证中心服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GovernanceAuthServiceApplication.class, args);
    }
}
