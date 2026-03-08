package com.governance.shared.web;

import java.util.Optional;

/**
 * 客户端请求上下文持有器。
 * <p>
 * 通过 {@link ThreadLocal} 在一次请求处理链路内缓存透传头信息，
 * 便于控制器、服务层、Feign 拦截器等组件在不直接依赖 Servlet API 的情况下获取请求上下文。
 */
public final class ClientRequestContextHolder {

    /**
     * 当前线程绑定的请求头上下文。
     */
    private static final ThreadLocal<ClientRequestHeaders> CONTEXT = new ThreadLocal<>();

    private ClientRequestContextHolder() {
    }

    /**
     * 设置当前线程的请求头上下文。
     *
     * @param headers 请求头对象
     */
    public static void set(ClientRequestHeaders headers) {
        CONTEXT.set(headers);
    }

    /**
     * 获取当前线程的请求头上下文。
     *
     * @return 请求头对象，可能为空
     */
    public static ClientRequestHeaders get() {
        return CONTEXT.get();
    }

    /**
     * 以 Optional 形式获取请求头上下文。
     *
     * @return Optional 包装结果
     */
    public static Optional<ClientRequestHeaders> getOptional() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * 清理当前线程绑定的请求头上下文，避免线程复用导致数据串扰。
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
