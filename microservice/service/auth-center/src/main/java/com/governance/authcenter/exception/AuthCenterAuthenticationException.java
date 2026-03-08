package com.governance.authcenter.exception;

/**
 * 认证中心认证失败异常。
 * <p>
 * 当用户名密码不匹配、令牌非法等认证失败场景发生时抛出，
 * 由全局异常处理器统一转换为 401 响应。
 */
public class AuthCenterAuthenticationException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public AuthCenterAuthenticationException(String message) {
        super(message);
    }
}
