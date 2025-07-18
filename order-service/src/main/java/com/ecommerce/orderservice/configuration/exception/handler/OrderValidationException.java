package com.ecommerce.orderservice.configuration.exception.handler;

import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ServiceException;

public class OrderValidationException extends ServiceException {
    public OrderValidationException(String message, Object... messageArgs) {
        super(ExceptionError.ORDER_VALIDATION, message, messageArgs);
    }
}