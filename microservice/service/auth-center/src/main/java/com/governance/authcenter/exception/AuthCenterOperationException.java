package com.governance.authcenter.exception;

/**
 * 认证中心业务操作异常。
 * <p>
 * 用于表示认证中心在注册、令牌处理或用户状态变更时的业务性错误，
 * 通常会被转换为 400 响应返回前端。
 */
public class AuthCenterOperationException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public AuthCenterOperationException(String message) {
        super(message);
    }
}
