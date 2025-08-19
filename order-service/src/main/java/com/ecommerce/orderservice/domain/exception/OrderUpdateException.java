package com.ecommerce.orderservice.domain.exception;

import com.ecommerce.shared.domain.exception.ExceptionError;
import com.ecommerce.shared.domain.exception.ServiceException;

public class OrderUpdateException extends ServiceException {
    public OrderUpdateException(String message, Object... messageArgs) {
        super(ExceptionError.ORDER_UPDATE_FAILED, message, messageArgs);
    }
}
