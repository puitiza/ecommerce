package com.ecommerce.productservice.configuration.exception.handler;

import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ServiceException;

public class ProductUpdateException extends ServiceException {

    public ProductUpdateException(String message, Object... messageArgs) {
        super(ExceptionError.PRODUCT_UPDATE_FAILED, message, messageArgs);
    }

}
