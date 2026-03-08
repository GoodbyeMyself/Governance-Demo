package com.governance.bms.user.exception;

/**
 * 后台用户禁用异常。
 * <p>
 * 当账号状态为禁用但仍尝试执行受限操作时抛出，
 * 由全局异常处理器统一映射为 403 响应。
 */
public class UserDisabledException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public UserDisabledException(String message) {
        super(message);
    }
}
