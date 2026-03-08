package com.governance.shared.exception;

/**
 * 资源不存在异常。
 * <p>
 * 当按主键、名称等条件查询业务资源但未命中时，
 * 可统一抛出该异常，并由全局异常处理器转换为 404 响应。
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
