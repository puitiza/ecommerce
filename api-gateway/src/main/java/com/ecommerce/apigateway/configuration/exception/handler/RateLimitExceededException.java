package com.ecommerce.apigateway.configuration.exception.handler;

import com.ecommerce.sharedlibrary.exception.HandledException;

/**
 * Exception thrown when the rate limit is exceeded.
 */
public class RateLimitExceededException extends HandledException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "EC-004";
    }
}
