package com.governance.metadata.exception;

/**
 * 重复元数据采集任务异常。
 * <p>
 * 当任务名称与已有任务冲突时抛出，
 * 由全局异常处理器映射为 409 响应。
 */
public class DuplicateMetadataCollectionTaskException extends RuntimeException {

    /**
     * 使用错误消息创建异常。
     *
     * @param message 错误消息
     */
    public DuplicateMetadataCollectionTaskException(String message) {
        super(message);
    }
}
