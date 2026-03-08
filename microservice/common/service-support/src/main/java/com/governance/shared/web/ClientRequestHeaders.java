package com.governance.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 客户端请求头模型。
 * <p>
 * 统一定义系统内跨服务透传的关键请求头字段，
 * 包括请求 ID、请求时间、来源应用、租户标识与认证信息。
 *
 * @param requestId     请求唯一标识
 * @param requestTime   请求发起时间
 * @param clientApp     客户端应用标识
 * @param tenantId      租户 ID
 * @param authorization 认证令牌
 */
public record ClientRequestHeaders(
        String requestId,
        String requestTime,
        String clientApp,
        String tenantId,
        String authorization
) {

    /**
     * 请求 ID 头名称。
     */
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    /**
     * 请求时间头名称。
     */
    public static final String HEADER_REQUEST_TIME = "X-Request-Time";

    /**
     * 客户端应用头名称。
     */
    public static final String HEADER_CLIENT_APP = "X-Client-App";

    /**
     * 租户 ID 头名称。
     */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    /**
     * Authorization 头名称。
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * 在 ServletRequest 中保存上下文对象时使用的属性名。
     */
    public static final String REQUEST_ATTRIBUTE = "clientRequestHeaders";

    /**
     * 从 HttpServletRequest 中解析并构造请求头对象。
     *
     * @param request Servlet 请求对象
     * @return 标准化后的请求头模型
     */
    public static ClientRequestHeaders from(HttpServletRequest request) {
        return new ClientRequestHeaders(
                normalize(request.getHeader(HEADER_REQUEST_ID)),
                normalize(request.getHeader(HEADER_REQUEST_TIME)),
                normalize(request.getHeader(HEADER_CLIENT_APP)),
                normalize(request.getHeader(HEADER_TENANT_ID)),
                normalize(request.getHeader(HEADER_AUTHORIZATION))
        );
    }

    /**
     * 生成一个带新请求 ID 的副本对象。
     *
     * @param newRequestId 新请求 ID
     * @return 新的请求头对象
     */
    public ClientRequestHeaders withRequestId(String newRequestId) {
        return new ClientRequestHeaders(
                normalize(newRequestId),
                requestTime,
                clientApp,
                tenantId,
                authorization
        );
    }

    /**
     * 标准化字符串，去除空白并将空值转换为 null。
     *
     * @param value 原始字符串
     * @return 标准化后的值
     */
    private static String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
