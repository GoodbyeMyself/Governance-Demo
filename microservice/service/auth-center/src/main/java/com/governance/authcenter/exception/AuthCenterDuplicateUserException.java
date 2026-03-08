package com.governance.authcenter.exception;

/**
 * 认证中心重复用户异常。
 * <p>
 * 当注册用户名、邮箱或手机号与现有账号冲突时抛出，
 * 由全局异常处理器转换为 409 响应。
 */
public class AuthCenterDuplicateUserException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public AuthCenterDuplicateUserException(String message) {
        super(message);
    }
}
