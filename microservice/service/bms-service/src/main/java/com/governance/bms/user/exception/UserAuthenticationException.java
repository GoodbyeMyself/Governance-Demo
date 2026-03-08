package com.governance.bms.user.exception;

/**
 * 后台用户认证失败异常。
 * <p>
 * 在用户名或密码校验失败时抛出，
 * 通常用于内部认证或统一登录场景。
 */
public class UserAuthenticationException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public UserAuthenticationException(String message) {
        super(message);
    }
}
