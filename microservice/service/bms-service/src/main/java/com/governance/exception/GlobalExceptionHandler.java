package com.governance.exception;

import com.governance.shared.exception.AbstractGlobalExceptionHandler;
import com.governance.shared.exception.ResourceNotFoundException;

import com.governance.bms.user.exception.DuplicateUserException;
import com.governance.bms.user.exception.UserAuthenticationException;
import com.governance.bms.user.exception.UserDisabledException;
import com.governance.bms.user.exception.UserOperationException;
import com.governance.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.governance")
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateUser(
            DuplicateUserException ex
    ) {
        return conflict(ex.getMessage());
    }

    @ExceptionHandler(UserAuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthFailed(
            UserAuthenticationException ex
    ) {
        return unauthorized(ex.getMessage());
    }

    @ExceptionHandler(UserDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserDisabled(
            UserDisabledException ex
    ) {
        return forbidden(ex.getMessage());
    }

    @ExceptionHandler(UserOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleOperationException(
            UserOperationException ex
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
