package com.governance.datasource.exception;

/**
 * 数据源被占用异常。
 * <p>
 * 当数据源仍被元数据任务引用，但尝试删除或进行不允许的修改时抛出。
 */
public class DataSourceInUseException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public DataSourceInUseException(String message) {
        super(message);
    }
}
