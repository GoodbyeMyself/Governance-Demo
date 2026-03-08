package com.governance.bms.user.exception;

/**
 * 后台用户业务操作异常。
 * <p>
 * 当用户管理过程中出现不满足业务规则的情况时抛出，
 * 例如非法角色变更、参数不完整等。
 */
public class UserOperationException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public UserOperationException(String message) {
        super(message);
    }
}
