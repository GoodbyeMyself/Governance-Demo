package com.governance.platform.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public record ClientRequestHeaders(
        String requestId,
        String requestTime,
        String clientApp,
        String tenantId,
        String authorization
) {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_REQUEST_TIME = "X-Request-Time";
    public static final String HEADER_CLIENT_APP = "X-Client-App";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String REQUEST_ATTRIBUTE = "clientRequestHeaders";

    public static ClientRequestHeaders from(HttpServletRequest request) {
        return new ClientRequestHeaders(
                normalize(request.getHeader(HEADER_REQUEST_ID)),
                normalize(request.getHeader(HEADER_REQUEST_TIME)),
                normalize(request.getHeader(HEADER_CLIENT_APP)),
                normalize(request.getHeader(HEADER_TENANT_ID)),
                normalize(request.getHeader(HEADER_AUTHORIZATION))
        );
    }

    public ClientRequestHeaders withRequestId(String newRequestId) {
        return new ClientRequestHeaders(
                normalize(newRequestId),
                requestTime,
                clientApp,
                tenantId,
                authorization
        );
    }

    private static String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
