package com.ecommerce.apigateway.configuration.exception.handler;

import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ServiceException;

/**
 * Exception thrown when the rate limit is exceeded.
 */
public class RateLimitExceededException extends ServiceException {

    public RateLimitExceededException(String message, Object... messageArgs) {
        super(ExceptionError.RATE_LIMIT_EXCEEDED, message, messageArgs);
    }
}
