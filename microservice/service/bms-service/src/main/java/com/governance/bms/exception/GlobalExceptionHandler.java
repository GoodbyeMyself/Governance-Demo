package com.governance.bms.exception;

import com.governance.bms.role.exception.RoleOperationException;
import com.governance.bms.user.exception.DuplicateUserException;
import com.governance.bms.user.exception.UserAuthenticationException;
import com.governance.bms.user.exception.UserDisabledException;
import com.governance.bms.user.exception.UserOperationException;
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
 * 基础管理服务全局异常处理器。
 *
 * <p>负责将后台用户管理与角色管理领域异常映射为统一的 HTTP 响应，
 * 让前端能够稳定处理各种业务失败场景。</p>
 */
@RestControllerAdvice(basePackages = "com.governance")
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    /**
     * 处理重复用户异常。
     *
     * @param ex 异常对象
     * @return 409 响应
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUser(
            DuplicateUserException ex
    ) {
        return conflict(ex.getMessage());
    }

    /**
     * 处理认证失败异常。
     *
     * @param ex 异常对象
     * @return 401 响应
     */
    @ExceptionHandler(UserAuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthFailed(
            UserAuthenticationException ex
    ) {
        return unauthorized(ex.getMessage());
    }

    /**
     * 处理用户禁用异常。
     *
     * @param ex 异常对象
     * @return 403 响应
     */
    @ExceptionHandler(UserDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserDisabled(
            UserDisabledException ex
    ) {
        return forbidden(ex.getMessage());
    }

    /**
     * 处理后台业务操作异常。
     *
     * @param ex 异常对象
     * @return 400 响应
     */
    @ExceptionHandler(UserOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleOperationException(
            UserOperationException ex
    ) {
        return badRequest(ex.getMessage());
    }

    /**
     * 处理角色定义操作异常。
     *
     * @param ex 异常对象
     * @return 400 响应
     */
    @ExceptionHandler(RoleOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleOperationException(
            RoleOperationException ex
    ) {
        return badRequest(ex.getMessage());
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
