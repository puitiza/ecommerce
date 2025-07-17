package com.ecommerce.shared.exception;

/**
 * Base exception class for handled exceptions.
 * Provides a method to retrieve a custom error code.
 */
public abstract class HandledException extends RuntimeException {
    protected HandledException(String message) {
        super(message);
    }

    protected HandledException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract String getErrorCode();
}


