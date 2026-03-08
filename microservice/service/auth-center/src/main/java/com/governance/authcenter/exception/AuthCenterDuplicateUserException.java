package com.governance.authcenter.exception;

public class AuthCenterDuplicateUserException extends RuntimeException {
    public AuthCenterDuplicateUserException(String message) {
        super(message);
    }
}

