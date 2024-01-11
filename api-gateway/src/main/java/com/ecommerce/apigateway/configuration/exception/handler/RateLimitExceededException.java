package com.ecommerce.apigateway.configuration.exception.handler;

public class RateLimitExceededException extends HandledException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return null;
    }
}
