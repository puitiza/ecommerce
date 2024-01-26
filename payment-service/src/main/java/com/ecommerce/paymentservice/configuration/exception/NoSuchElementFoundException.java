package com.ecommerce.paymentservice.configuration.exception;

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
