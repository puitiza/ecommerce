package com.ecommerce.productservice.configuration.exception.handler;

import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ServiceException;

public class ResourceNotFoundException extends ServiceException {
    public ResourceNotFoundException(String resourceType, String id) {
        super(ExceptionError.NOT_FOUND, null, resourceType, id);
    }
}