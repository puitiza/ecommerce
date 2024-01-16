package com.ecommerce.orderservice.configuration.exception.handler;

public class ProductRetrievalException extends HandledException{
    public ProductRetrievalException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return null;
    }
}
