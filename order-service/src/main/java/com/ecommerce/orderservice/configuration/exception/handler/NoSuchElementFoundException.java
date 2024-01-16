package com.ecommerce.orderservice.configuration.exception.handler;

public class NoSuchElementFoundException extends HandledException {

    String code;

    public NoSuchElementFoundException(String message, String code) {
        super(message);
        this.code = code;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

}
