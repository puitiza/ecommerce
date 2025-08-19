package com.ecommerce.productservice.configuration.exception.handler;

import com.ecommerce.shared.domain.exception.ExceptionError;
import com.ecommerce.shared.domain.exception.ServiceException;

public class InvalidInventoryException extends ServiceException {
    public InvalidInventoryException(String message, Object... messageArgs) {
        super(ExceptionError.PRODUCT_INVALID_INVENTORY, message, messageArgs);
    }
}
