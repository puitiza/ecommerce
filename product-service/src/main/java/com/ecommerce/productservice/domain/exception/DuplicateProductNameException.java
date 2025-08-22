package com.ecommerce.productservice.domain.exception;

import com.ecommerce.shared.domain.exception.ExceptionError;
import com.ecommerce.shared.domain.exception.ServiceException;

public class DuplicateProductNameException extends ServiceException {
    public DuplicateProductNameException(String message, Object... messageArgs) {
        super(ExceptionError.PRODUCT_DUPLICATE_NAME, message, messageArgs);
    }
}
