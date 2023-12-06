package com.ecommerce.authservice.component.exception.handler;

public class TokenRefreshException extends HandledException {

    private final String code;

    public TokenRefreshException(String message, String code) {
        super(message);
        this.code = code;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

}
