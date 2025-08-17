package com.ecommerce.userservice.configuration.exception.handler;

import com.ecommerce.shared.domain.exception.ExceptionError;
import com.ecommerce.shared.domain.exception.ServiceException;

public class InvalidUserException extends ServiceException {

    public InvalidUserException(String message, Object... messageArgs) {
        super(ExceptionError.USER_USERNAME_FOUND, message, messageArgs);
    }
}
