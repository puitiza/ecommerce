package com.ecommerce.orderservice.configuration.exception.handler;

import com.ecommerce.shared.exception.HandledException;

public class ProductRetrievalException extends HandledException {
    public ProductRetrievalException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return null;
    }
}
