package com.ecommerce.productservice.domain.exception;

import com.ecommerce.shared.domain.exception.ExceptionError;
import com.ecommerce.shared.domain.exception.ServiceException;

public class InvalidProductDataException extends ServiceException {
    public InvalidProductDataException(String message, Object... messageArgs) {
        super(ExceptionError.PRODUCT_VALIDATION, message, messageArgs);
    }
}
