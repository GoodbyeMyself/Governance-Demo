package com.governance.bms.role.exception;

/**
 * 角色定义操作异常。
 *
 * <p>当角色定义维护过程中违反业务规则时抛出，
 * 例如尝试修改内置管理员角色等。</p>
 */
public class RoleOperationException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public RoleOperationException(String message) {
        super(message);
    }
}
