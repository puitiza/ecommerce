package com.ecommerce.productservice.configuration.exception.handler;

public class ProductUpdateException extends HandledException {
    public ProductUpdateException(String message, Throwable exception) {
        super(message, exception);
    }

    @Override
    public String getErrorCode() {
        return null;
    }
}
