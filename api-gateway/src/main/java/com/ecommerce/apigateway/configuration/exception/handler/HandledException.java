package com.ecommerce.apigateway.configuration.exception.handler;

public abstract class HandledException extends RuntimeException {

    protected HandledException(String message) {
        super(message);
    }

    public abstract String getErrorCode();

}
