package com.governance.shared.web;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 客户端请求头提供器。
 * <p>
 * 为业务代码提供简洁的读取入口，避免各处直接访问 {@link ClientRequestContextHolder}，
 * 也便于后续替换实现或扩展默认值策略。
 */
@Component
public class ClientRequestHeaderProvider {

    /**
     * 获取当前请求头上下文。
     *
     * @return Optional 包装的请求头对象
     */
    public Optional<ClientRequestHeaders> getHeaders() {
        return ClientRequestContextHolder.getOptional();
    }

    /**
     * 获取请求 ID。
     *
     * @return 请求 ID，不存在时返回 null
     */
    public String getRequestId() {
        return getHeaders().map(ClientRequestHeaders::requestId).orElse(null);
    }

    /**
     * 获取请求时间头。
     *
     * @return 请求时间，不存在时返回 null
     */
    public String getRequestTime() {
        return getHeaders().map(ClientRequestHeaders::requestTime).orElse(null);
    }

    /**
     * 获取客户端应用标识。
     *
     * @return 客户端应用标识，不存在时返回 null
     */
    public String getClientApp() {
        return getHeaders().map(ClientRequestHeaders::clientApp).orElse(null);
    }

    /**
     * 获取租户 ID。
     *
     * @return 租户 ID，不存在时返回 null
     */
    public String getTenantId() {
        return getHeaders().map(ClientRequestHeaders::tenantId).orElse(null);
    }

    /**
     * 获取 Authorization 头。
     *
     * @return Authorization 内容，不存在时返回 null
     */
    public String getAuthorization() {
        return getHeaders().map(ClientRequestHeaders::authorization).orElse(null);
    }
}
