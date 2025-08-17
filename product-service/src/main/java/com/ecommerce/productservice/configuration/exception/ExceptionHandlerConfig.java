package com.ecommerce.productservice.configuration.exception;

import com.ecommerce.productservice.configuration.exception.handler.InvalidInventoryException;
import com.ecommerce.productservice.configuration.exception.handler.ProductUpdateException;
import com.ecommerce.shared.exception.ErrorResponseBuilder;
import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.GlobalExceptionHandler;
import com.ecommerce.shared.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionHandlerConfig extends GlobalExceptionHandler {
    public ExceptionHandlerConfig(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @ExceptionHandler(InvalidInventoryException.class)
    public ResponseEntity<Object> handleInvalidInventoryException(ServiceException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ExceptionError.PRODUCT_INVALID_INVENTORY, null, ex.getMessage());
    }

    @ExceptionHandler(ProductUpdateException.class)
    public ResponseEntity<Object> handleProductUpdateException(ServiceException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ExceptionError.PRODUCT_UPDATE_FAILED, null, ex.getMessage());
    }
}