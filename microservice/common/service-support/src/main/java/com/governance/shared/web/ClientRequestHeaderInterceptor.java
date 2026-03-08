package com.governance.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 客户端请求头拦截器。
 * <p>
 * 在请求进入 Spring MVC 处理链之前读取并标准化透传头，
 * 同时补齐缺失的请求 ID，方便链路追踪和跨服务传递上下文。
 */
@Component
public class ClientRequestHeaderInterceptor implements HandlerInterceptor {

    /**
     * 在控制器执行前提取并缓存请求头上下文。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  处理器对象
     * @return 始终返回 true，继续后续链路
     */
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

    /**
     * 请求完成后清理线程上下文，防止线程池复用导致脏数据残留。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  处理器对象
     * @param ex       处理过程中抛出的异常
     */
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
