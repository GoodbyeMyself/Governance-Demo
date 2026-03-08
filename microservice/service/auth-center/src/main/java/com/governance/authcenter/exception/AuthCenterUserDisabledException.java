package com.governance.authcenter.exception;

/**
 * 认证中心用户禁用异常。
 * <p>
 * 当账号状态为禁用但仍尝试登录或访问受保护资源时抛出，
 * 由全局异常处理器统一映射为 403 响应。
 */
public class AuthCenterUserDisabledException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public AuthCenterUserDisabledException(String message) {
        super(message);
    }
}
