package com.ecommerce.orderservice.configuration.exception.handler;

import com.ecommerce.shared.exception.HandledException;

public class ResourceNotFoundException extends HandledException {

    String code;

    public ResourceNotFoundException(String message, String code) {
        super(message);
        this.code = code;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

}
