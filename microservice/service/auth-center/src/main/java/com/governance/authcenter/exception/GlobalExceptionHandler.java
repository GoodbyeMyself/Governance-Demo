package com.governance.authcenter.exception;

import com.governance.authcenter.exception.AuthCenterAuthenticationException;
import com.governance.authcenter.exception.AuthCenterDuplicateUserException;
import com.governance.authcenter.exception.AuthCenterOperationException;
import com.governance.authcenter.exception.AuthCenterUserDisabledException;
import com.governance.shared.api.ApiResponse;
import com.governance.shared.exception.AbstractGlobalExceptionHandler;
import com.governance.shared.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/**
 * 认证中心全局异常处理器。
 * <p>
 * 在公共异常处理基类基础上，补充认证中心特有的认证失败、
 * 账号禁用、重复注册等领域异常映射规则。
 */
@RestControllerAdvice(basePackages = "com.governance")
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    /**
     * 处理重复用户异常。
     *
     * @param ex 异常对象
     * @return 409 响应
     */
    @ExceptionHandler(AuthCenterDuplicateUserException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUser(
            AuthCenterDuplicateUserException ex
    ) {
        return conflict(ex.getMessage());
    }

    /**
     * 处理认证失败异常。
     *
     * @param ex 异常对象
     * @return 401 响应
     */
    @ExceptionHandler(AuthCenterAuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthFailed(
            AuthCenterAuthenticationException ex
    ) {
        return unauthorized(ex.getMessage());
    }

    /**
     * 处理认证信息缺失异常。
     *
     * @param ex 异常对象
     * @return 401 响应
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthCredentialsMissing(
            AuthenticationCredentialsNotFoundException ex
    ) {
        return unauthorized(message("security.unauthenticated"));
    }

    /**
     * 处理无权限异常。
     *
     * @param ex 异常对象
     * @return 403 响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex
    ) {
        return forbidden(message("security.accessDenied"));
    }

    /**
     * 处理账号禁用异常。
     *
     * @param ex 异常对象
     * @return 403 响应
     */
    @ExceptionHandler(AuthCenterUserDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserDisabled(
            AuthCenterUserDisabledException ex
    ) {
        return forbidden(ex.getMessage());
    }

    /**
     * 处理认证中心业务异常。
     *
     * @param ex 异常对象
     * @return 400 响应
     */
    @ExceptionHandler(AuthCenterOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleOperationException(
            AuthCenterOperationException ex
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
     * @return 字段校验错误信息
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
