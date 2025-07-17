package com.ecommerce.orderservice.configuration.exception;

import com.ecommerce.orderservice.configuration.exception.handler.OrderCancellationException;
import com.ecommerce.orderservice.configuration.exception.handler.OrderValidationException;
import com.ecommerce.orderservice.configuration.exception.handler.ProductRetrievalException;
import com.ecommerce.orderservice.configuration.exception.handler.ResourceNotFoundException;
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

    @ExceptionHandler({ResourceNotFoundException.class, ProductRetrievalException.class})
    public ResponseEntity<Object> handleNotFoundException(Exception ex, WebRequest request) {
        log.error("Failed to find the requested element", ex);
        return errorResponseBuilder.structure(ex, HttpStatus.NOT_FOUND, request, ErrorCodes.ORDER_NOT_FOUND);
    }

    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<Object> handleOrderValidationException(OrderValidationException ex, WebRequest request) {
        log.error("Order validation failed: {}. Details: {}", ex.getMessage(), ex.toString());
        return errorResponseBuilder.structure(ex, HttpStatus.BAD_REQUEST, request, ErrorCodes.ORDER_VALIDATION);
    }

    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<Object> handleOrderCancellationException(OrderCancellationException ex, WebRequest request) {
        log.error("Failed to cancel the order: " + ex.getMessage() + "{}", ex);
        return errorResponseBuilder.structure(ex, HttpStatus.BAD_REQUEST, request, ErrorCodes.ORDER_CANCELLATION);
    }

}
