package com.ecommerce.productservice.configuration.exception.handler;

public class InvalidInventoryException extends HandledException{
    public InvalidInventoryException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return null;
    }
}
