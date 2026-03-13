package com.governance.iotdevice.exception;

/**
 * 重复 IoT 设备异常。
 */
public class DuplicateIotDeviceException extends RuntimeException {

    public DuplicateIotDeviceException(String message) {
        super(message);
    }
}
