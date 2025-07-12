package com.ecommerce.apigateway.configuration.exception.handler;

/**
 * Base exception class for handled exceptions in the API Gateway.
 * Provides a method to retrieve a custom error code.
 */
public abstract class HandledException extends RuntimeException {

    protected HandledException(String message) {
        super(message);
    }

    /**
     * Returns the custom error code for the exception.
     *
     * @return the error code
     */
    public abstract String getErrorCode();

}
