package com.ecommerce.apigateway.configuration.exception.handler;

import com.ecommerce.shared.domain.exception.ExceptionError;
import com.ecommerce.shared.domain.exception.ServiceException;

public class RateLimitExceededException extends ServiceException {

    public RateLimitExceededException(String message, Object... messageArgs) {
        super(ExceptionError.GATEWAY_RATE_LIMIT, message, messageArgs);
    }
}
