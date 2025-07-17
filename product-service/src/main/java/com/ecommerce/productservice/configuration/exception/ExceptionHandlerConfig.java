package com.ecommerce.productservice.configuration.exception;

import com.ecommerce.productservice.configuration.exception.handler.InvalidInventoryException;
import com.ecommerce.productservice.configuration.exception.handler.NoSuchElementFoundException;
import com.ecommerce.productservice.configuration.exception.handler.ProductUpdateException;
import com.ecommerce.shared.exception.ErrorCodes;
import com.ecommerce.shared.exception.ErrorResponseBuilder;
import com.ecommerce.shared.exception.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
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

    @ExceptionHandler(NoSuchElementFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(Exception ex, WebRequest request) {
        return errorResponseBuilder.structure(ex, HttpStatus.NOT_FOUND, request, ErrorCodes.PRODUCT_NOT_FOUND);
    }

    @ExceptionHandler(InvalidInventoryException.class)
    public ResponseEntity<Object> handleInvalidInventoryException(Exception ex, WebRequest request) {
        return errorResponseBuilder.structure(ex, HttpStatus.BAD_REQUEST, request, ErrorCodes.PRODUCT_INVALID_INVENTORY);
    }

    @ExceptionHandler(ProductUpdateException.class)
    public ResponseEntity<Object> handleProductUpdateException(Exception ex, WebRequest request) {
        return errorResponseBuilder.structure(ex, HttpStatus.INTERNAL_SERVER_ERROR, request, ErrorCodes.PRODUCT_UPDATE_FAILED);
    }
}
