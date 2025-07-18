package com.ecommerce.productservice.configuration.exception.handler;

import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ServiceException;

public class ResourceNotFoundException extends ServiceException {

    public ResourceNotFoundException(String message, Object... messageArgs) {
        super(ExceptionError.NOT_FOUND, message, messageArgs);
    }
}
