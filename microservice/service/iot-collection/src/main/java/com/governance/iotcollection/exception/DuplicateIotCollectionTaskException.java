package com.governance.iotcollection.exception;

/**
 * 重复 IoT 采集任务异常。
 */
public class DuplicateIotCollectionTaskException extends RuntimeException {

    public DuplicateIotCollectionTaskException(String message) {
        super(message);
    }
}
