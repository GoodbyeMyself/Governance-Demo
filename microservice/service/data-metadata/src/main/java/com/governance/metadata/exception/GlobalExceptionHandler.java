package com.governance.metadata.exception;

import com.governance.metadata.exception.DuplicateMetadataCollectionTaskException;
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
 * 元数据服务全局异常处理器。
 * <p>
 * 用于处理采集任务重复等领域异常，并复用公共参数校验和兜底异常逻辑。
 */
@RestControllerAdvice(basePackages = "com.governance")
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    /**
     * 处理重复采集任务异常。
     *
     * @param ex 异常对象
     * @return 409 响应
     */
    @ExceptionHandler(DuplicateMetadataCollectionTaskException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateTask(
            DuplicateMetadataCollectionTaskException ex
    ) {
        return conflict(ex.getMessage());
    }

    /**
     * 处理资源不存在异常。
     *
     * @param ex 异常对象
     * @return 404 响应
     */
    @Override
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex
    ) {
        return super.handleResourceNotFound(ex);
    }

    /**
     * 处理参数校验失败异常。
     *
     * @param ex 异常对象
     * @return 字段级错误信息
     */
    @Override
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        return super.handleValidation(ex);
    }

    /**
     * 处理参数类型不匹配异常。
     *
     * @param ex 异常对象
     * @return 400 响应
     */
    @Override
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        return super.handleTypeMismatch(ex);
    }

    /**
     * 处理非法参数异常。
     *
     * @param ex 异常对象
     * @return 400 响应
     */
    @Override
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        return super.handleIllegalArgument(ex);
    }

    /**
     * 处理兜底异常。
     *
     * @param ex 异常对象
     * @return 500 响应
     */
    @Override
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex
    ) {
        return super.handleGenericException(ex);
    }
}
