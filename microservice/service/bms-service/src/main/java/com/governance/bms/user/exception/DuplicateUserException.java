package com.governance.bms.user.exception;

/**
 * 后台用户重复异常。
 * <p>
 * 当用户名、邮箱或手机号与现有用户冲突时抛出，
 * 由全局异常处理器转换为 409 响应。
 */
public class DuplicateUserException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public DuplicateUserException(String message) {
        super(message);
    }
}
