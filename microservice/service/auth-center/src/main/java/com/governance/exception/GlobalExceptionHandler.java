package com.governance.exception;

import com.governance.shared.exception.AbstractGlobalExceptionHandler;
import com.governance.shared.exception.ResourceNotFoundException;

import com.governance.authcenter.exception.AuthCenterAuthenticationException;
import com.governance.authcenter.exception.AuthCenterDuplicateUserException;
import com.governance.authcenter.exception.AuthCenterOperationException;
import com.governance.authcenter.exception.AuthCenterUserDisabledException;
import com.governance.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.governance")
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    @ExceptionHandler(AuthCenterDuplicateUserException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUser(
            AuthCenterDuplicateUserException ex
    ) {
        return conflict(ex.getMessage());
    }

    @ExceptionHandler(AuthCenterAuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthFailed(
            AuthCenterAuthenticationException ex
    ) {
        return unauthorized(ex.getMessage());
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthCredentialsMissing(
            AuthenticationCredentialsNotFoundException ex
    ) {
        return unauthorized("Authentication required or token expired");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex
    ) {
        return forbidden("Access denied");
    }

    @ExceptionHandler(AuthCenterUserDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserDisabled(
            AuthCenterUserDisabledException ex
    ) {
        return forbidden(ex.getMessage());
    }

    @ExceptionHandler(AuthCenterOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleOperationException(
            AuthCenterOperationException ex
    ) {
        return badRequest(ex.getMessage());
    }

    @Override
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex
    ) {
        return super.handleResourceNotFound(ex);
    }

    @Override
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        return super.handleValidation(ex);
    }

    @Override
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        return super.handleTypeMismatch(ex);
    }

    @Override
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        return super.handleIllegalArgument(ex);
    }

    @Override
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex
    ) {
        return super.handleGenericException(ex);
    }
}
