package com.governance.iotdevice.exception;

import com.governance.shared.api.ApiResponse;
import com.governance.shared.exception.AbstractGlobalExceptionHandler;
import com.governance.shared.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/**
 * IoT 设备服务全局异常处理器。
 */
@RestControllerAdvice(basePackages = "com.governance")
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    @ExceptionHandler(DuplicateIotDeviceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateIotDevice(DuplicateIotDeviceException ex) {
        return conflict(ex.getMessage());
    }

    @ExceptionHandler(IotDeviceInUseException.class)
    public ResponseEntity<ApiResponse<Void>> handleIotDeviceInUse(IotDeviceInUseException ex) {
        return conflict(ex.getMessage());
    }

    @Override
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return super.handleResourceNotFound(ex);
    }

    @Override
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        return super.handleValidation(ex);
    }

    @Override
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return super.handleTypeMismatch(ex);
    }

    @Override
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return super.handleIllegalArgument(ex);
    }

    @Override
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return super.handleGenericException(ex);
    }
}
