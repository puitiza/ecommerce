package com.ecommerce.authservice.component.exception.handler;

public class ExistingElementFoundException extends HandledException {

    private final String code;

    public ExistingElementFoundException(String message, String code) {
        super(message);
        this.code = code;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

}
