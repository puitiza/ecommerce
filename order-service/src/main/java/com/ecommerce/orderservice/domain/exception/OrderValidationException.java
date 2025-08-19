package com.ecommerce.orderservice.domain.exception;

import com.ecommerce.shared.domain.exception.ExceptionError;
import com.ecommerce.shared.domain.exception.ServiceException;

public class OrderValidationException extends ServiceException {
    public OrderValidationException(String message, Object... messageArgs) {
        super(ExceptionError.ORDER_VALIDATION, message, messageArgs);
    }

    public OrderValidationException(ExceptionError error, Object... messageArgs) {
        super(error, null, messageArgs);
    }

    public OrderValidationException(ExceptionError error, String message) {
        super(error, message);
    }

    public OrderValidationException(ExceptionError error, String message, Object... messageArgs) {
        super(error, message, messageArgs);
    }
}