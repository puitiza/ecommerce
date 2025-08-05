package com.ecommerce.orderservice.infrastructure.configuration.exception;

import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ServiceException;

public class OrderCancellationException extends ServiceException {
    public OrderCancellationException(String message, Object... messageArgs) {
        super(ExceptionError.ORDER_CANCELLATION, message, messageArgs);
    }
}