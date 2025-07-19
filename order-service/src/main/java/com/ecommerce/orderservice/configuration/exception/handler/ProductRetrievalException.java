package com.ecommerce.orderservice.configuration.exception.handler;

import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ServiceException;

public class ProductRetrievalException extends ServiceException {
    public ProductRetrievalException(String resourceType, String id) {
        super(ExceptionError.NOT_FOUND, null, resourceType, id);
    }
}