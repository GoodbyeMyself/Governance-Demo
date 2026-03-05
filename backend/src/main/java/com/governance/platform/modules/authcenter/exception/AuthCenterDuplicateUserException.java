package com.governance.platform.modules.authcenter.exception;

public class AuthCenterDuplicateUserException extends RuntimeException {
    public AuthCenterDuplicateUserException(String message) {
        super(message);
    }
}

