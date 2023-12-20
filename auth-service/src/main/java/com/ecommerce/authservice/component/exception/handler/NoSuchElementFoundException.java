package com.ecommerce.authservice.component.exception.handler;

public class NoSuchElementFoundException extends HandledException {

    private final String code;

    public NoSuchElementFoundException(String message, String code) {
        super(message);
        this.code = code;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

}
