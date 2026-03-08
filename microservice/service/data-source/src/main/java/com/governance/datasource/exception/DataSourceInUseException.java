package com.governance.datasource.exception;

public class DataSourceInUseException extends RuntimeException {
    public DataSourceInUseException(String message) {
        super(message);
    }
}



