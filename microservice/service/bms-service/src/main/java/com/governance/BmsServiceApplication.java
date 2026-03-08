package com.governance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 基础管理服务启动入口。
 *
 * <p>该服务负责用户、角色与权限等基础管理能力，
 * 同时为认证中心提供内部用户查询接口。</p>
 */
@SpringBootApplication
public class BmsServiceApplication {

    /**
     * 启动基础管理服务。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(BmsServiceApplication.class, args);
    }
}

