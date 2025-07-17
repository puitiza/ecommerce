package com.ecommerce.userservice.configuration.exception.handler;

import com.ecommerce.shared.exception.HandledException;

public class InvalidUserException extends HandledException {

    public InvalidUserException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return null;
    }
}
