package com.governance.datasource.exception;

/**
 * 重复数据源异常。
 * <p>
 * 当数据源名称与现有记录冲突时抛出，
 * 由全局异常处理器映射为 409 响应。
 */
public class DuplicateDataSourceException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public DuplicateDataSourceException(String message) {
        super(message);
    }
}
