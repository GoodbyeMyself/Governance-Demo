package com.governance.shared.exception;

import com.governance.shared.api.ApiResponse;
import com.governance.shared.i18n.MessageResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理基类。
 * <p>
 * 各业务服务通过继承该类复用统一的异常响应格式，
 * 并在子类中按需补充领域异常的映射规则，
 * 从而保持整个平台的错误码语义和返回体结构一致。
 */
public abstract class AbstractGlobalExceptionHandler {

    @Autowired
    private MessageResolver messageResolver;

    /**
     * 解析国际化消息。
     *
     * @param code 消息编码
     * @param args 占位参数
     * @return 本地化后的消息
     */
    protected String message(String code, Object... args) {
        return messageResolver.getMessage(code, args);
    }

    /**
     * 构造 409 冲突响应。
     *
     * @param message 错误消息
     * @return 响应实体
     */
    protected ResponseEntity<ApiResponse<Void>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(message));
    }

    /**
     * 构造 401 未认证响应。
     *
     * @param message 错误消息
     * @return 响应实体
     */
    protected ResponseEntity<ApiResponse<Void>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(message));
    }

    /**
     * 构造 403 无权限响应。
     *
     * @param message 错误消息
     * @return 响应实体
     */
    protected ResponseEntity<ApiResponse<Void>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(message));
    }

    /**
     * 构造 400 参数错误响应。
     *
     * @param message 错误消息
     * @return 响应实体
     */
    protected ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(message));
    }

    /**
     * 构造 404 资源不存在响应。
     *
     * @param message 错误消息
     * @return 响应实体
     */
    protected ResponseEntity<ApiResponse<Void>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(message));
    }

    /**
     * 构造 500 服务器内部错误响应。
     *
     * @param message 错误消息
     * @return 响应实体
     */
    protected ResponseEntity<ApiResponse<Void>> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(message));
    }

    /**
     * 处理资源不存在异常。
     *
     * @param ex 异常对象
     * @return 404 响应
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return notFound(ex.getMessage());
    }

    /**
     * 处理 Bean Validation 校验失败异常。
     *
     * @param ex 异常对象
     * @return 字段级错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(message("common.validation.failed"), errors));
    }

    /**
     * 处理请求参数类型不匹配异常。
     *
     * @param ex 异常对象
     * @return 400 响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        return badRequest(message("common.request.parameter.invalid", ex.getName()));
    }

    /**
     * 处理请求体不可读异常。
     *
     * @param ex 异常对象
     * @return 400 响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(
            HttpMessageNotReadableException ex
    ) {
        return badRequest(message("common.request.body.invalid"));
    }

    /**
     * 处理通用非法参数异常。
     *
     * @param ex 异常对象
     * @return 400 响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        return badRequest(ex.getMessage());
    }

    /**
     * 处理数据库访问异常。
     *
     * @param ex 异常对象
     * @return 500 响应
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException ex) {
        return internalServerError(message("common.database.failed"));
    }

    /**
     * 处理未被显式捕获的兜底异常。
     *
     * @param ex 异常对象
     * @return 500 响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return internalServerError(message("common.internal.error"));
    }
}
