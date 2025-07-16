package com.ecommerce.orderservice.configuration.exception.handler;

import com.ecommerce.shared.exception.HandledException;

public class OrderCancellationException extends HandledException {
    public OrderCancellationException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return null;
    }
}
