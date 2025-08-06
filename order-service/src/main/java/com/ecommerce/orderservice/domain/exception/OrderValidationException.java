package com.ecommerce.orderservice.domain.exception;

import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ServiceException;

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

    public OrderValidationException(ExceptionError error, String message, Throwable cause, Object... messageArgs) {
        super(error, message, cause, messageArgs);
    }
}