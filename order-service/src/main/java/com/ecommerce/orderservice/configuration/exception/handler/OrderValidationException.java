package com.ecommerce.orderservice.configuration.exception.handler;

import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ServiceException;

public class OrderValidationException extends ServiceException {

    public OrderValidationException(String id) {
        super(ExceptionError.ORDER_NOT_FOUND, "Order not found", id);
    }
}
