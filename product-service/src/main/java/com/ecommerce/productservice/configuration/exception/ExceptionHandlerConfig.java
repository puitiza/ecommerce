package com.ecommerce.productservice.configuration.exception;

import com.ecommerce.productservice.configuration.exception.handler.InvalidInventoryException;
import com.ecommerce.productservice.configuration.exception.handler.ProductUpdateException;
import com.ecommerce.productservice.configuration.exception.handler.ResourceNotFoundException;
import com.ecommerce.shared.exception.ErrorResponseBuilder;
import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.GlobalExceptionHandler;
import com.ecommerce.shared.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
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

    public ExceptionHandlerConfig(ErrorResponseBuilder errorResponseBuilder, MessageSource messageSource) {
        super(errorResponseBuilder, messageSource);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(ServiceException ex, WebRequest request) {
        var body = errorResponseBuilder.structure(ex, HttpStatus.NOT_FOUND, ExceptionError.ORDER_NOT_FOUND, ex.getMessage(), ex.getMessageArgs());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidInventoryException.class)
    public ResponseEntity<Object> handleInvalidInventoryException(ServiceException ex, WebRequest request) {
        var body = errorResponseBuilder.structure(ex, HttpStatus.BAD_REQUEST, ExceptionError.PRODUCT_INVALID_INVENTORY, ex.getMessage(), ex.getMessageArgs());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ProductUpdateException.class)
    public ResponseEntity<Object> handleProductUpdateException(ServiceException ex, WebRequest request) {
        var body = errorResponseBuilder.structure(ex, HttpStatus.BAD_REQUEST, ExceptionError.PRODUCT_UPDATE_FAILED, ex.getMessage(), ex.getMessageArgs());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
