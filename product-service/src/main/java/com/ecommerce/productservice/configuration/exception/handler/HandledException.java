package com.ecommerce.productservice.configuration.exception.handler;

public abstract class HandledException extends RuntimeException {

    protected HandledException(String message, Throwable exception) {
        super(message, exception);
    }

    protected HandledException(String message) {
        super(message);
    }

    public abstract String getErrorCode();

}
