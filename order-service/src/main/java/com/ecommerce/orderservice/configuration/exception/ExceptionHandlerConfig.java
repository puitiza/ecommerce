package com.ecommerce.orderservice.configuration.exception;

import com.ecommerce.orderservice.configuration.exception.handler.OrderCancellationException;
import com.ecommerce.orderservice.configuration.exception.handler.OrderValidationException;
import com.ecommerce.orderservice.configuration.exception.handler.ProductRetrievalException;
import com.ecommerce.orderservice.configuration.exception.handler.ResourceNotFoundException;
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

    @ExceptionHandler({ResourceNotFoundException.class, ProductRetrievalException.class})
    public ResponseEntity<Object> handleNotFoundException(ServiceException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, HttpStatus.NOT_FOUND, request, ExceptionError.NOT_FOUND, ex.getMessageArgs());
    }

    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<Object> handleOrderValidationException(OrderValidationException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, HttpStatus.BAD_REQUEST, request, ExceptionError.ORDER_VALIDATION, ex.getMessageArgs());
    }

    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<Object> handleOrderCancellationException(OrderCancellationException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, HttpStatus.BAD_REQUEST, request, ExceptionError.ORDER_CANCELLATION, ex.getMessageArgs());
    }
}