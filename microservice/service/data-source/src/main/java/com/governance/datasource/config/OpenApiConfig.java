package com.governance.datasource.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 数据源服务 OpenAPI 配置。
 * <p>
 * 用于描述数据源新增、更新、删除、查询等接口文档，
 * 并统一声明 Bearer Token 鉴权方式。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 构建数据源服务 OpenAPI 对象。
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI governanceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Governance Platform - Data Source API")
                        .description("Data source create, update, delete and query APIs")
                        .version("v1"))
                .servers(List.of(new Server().url("/").description("Current host")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
