package com.ecommerce.orderservice.configuration.exception.handler;

public class OrderCreationException extends HandledException{

    public OrderCreationException(String message, Throwable e) {
        super(message, e);
    }

    @Override
    public String getErrorCode() {
        return null;
    }
}
