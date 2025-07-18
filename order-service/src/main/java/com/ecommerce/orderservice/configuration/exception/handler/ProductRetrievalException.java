package com.ecommerce.orderservice.configuration.exception.handler;

import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ServiceException;

public class ProductRetrievalException extends ServiceException {
    public ProductRetrievalException(String message, Object... messageArgs) {
        super(ExceptionError.PRODUCT_UPDATE_FAILED, message, messageArgs);
    }
}
