package com.ecommerce.productservice.interfaces.rest;

import com.ecommerce.productservice.domain.exception.DuplicateProductNameException;
import com.ecommerce.productservice.domain.exception.InvalidProductDataException;
import com.ecommerce.shared.application.exception.ErrorResponseBuilder;
import com.ecommerce.shared.application.exception.GlobalExceptionHandler;
import com.ecommerce.shared.domain.exception.ExceptionError;
import com.ecommerce.shared.domain.exception.ServiceException;
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
public class ProductExceptionHandler extends GlobalExceptionHandler {
    public ProductExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    @ExceptionHandler(DuplicateProductNameException.class)
    public ResponseEntity<Object> handleDuplicateProductNameException(ServiceException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ExceptionError.PRODUCT_DUPLICATE_NAME, null, ex.getMessage());
    }

    @ExceptionHandler(InvalidProductDataException.class)
    public ResponseEntity<Object> handleInvalidProductDataException(ServiceException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ExceptionError.PRODUCT_VALIDATION, null, ex.getMessage());
    }
}