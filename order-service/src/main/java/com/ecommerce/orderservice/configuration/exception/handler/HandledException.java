package com.ecommerce.orderservice.configuration.exception.handler;

public abstract class HandledException extends RuntimeException {

    protected HandledException(String message, Throwable e) {
        super(message, e);
    }

    protected HandledException(String message) {
        super(message);
    }

    public abstract String getErrorCode();

}
