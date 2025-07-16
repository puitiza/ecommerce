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

    /**
     * Returns the custom error code for the exception.
     *
     * @return the error code
     */
    public abstract String getErrorCode();
}


