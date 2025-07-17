package com.ecommerce.productservice.configuration.exception.handler;

import com.ecommerce.shared.exception.HandledException;

public class NoSuchElementFoundException extends HandledException {

    String code;

    public NoSuchElementFoundException(String message, String code) {
        super(message);
        this.code = code;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

}
