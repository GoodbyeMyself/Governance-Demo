package com.governance.platform.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class ClientRequestHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        ClientRequestHeaders headers = ClientRequestHeaders.from(request);

        if (headers.requestId() == null) {
            headers = headers.withRequestId(UUID.randomUUID().toString());
        }

        ClientRequestContextHolder.set(headers);
        request.setAttribute(ClientRequestHeaders.REQUEST_ATTRIBUTE, headers);
        response.setHeader(ClientRequestHeaders.HEADER_REQUEST_ID, headers.requestId());
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex
    ) {
        ClientRequestContextHolder.clear();
    }
}
