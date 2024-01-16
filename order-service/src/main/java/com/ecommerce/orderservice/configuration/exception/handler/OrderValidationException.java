package com.ecommerce.orderservice.configuration.exception.handler;

public class OrderValidationException extends HandledException{

    public OrderValidationException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return null;
    }
}
