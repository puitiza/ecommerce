package com.ecommerce.orderservice.configuration.exception;

import com.ecommerce.orderservice.configuration.exception.handler.OrderCancellationException;
import com.ecommerce.orderservice.configuration.exception.handler.OrderValidationException;
import com.ecommerce.shared.exception.ErrorResponseBuilder;
import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.GlobalExceptionHandler;
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

    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<Object> handleOrderValidationException(OrderValidationException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ex.getError(), null, ex.getMessageArgs());
    }

    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<Object> handleOrderCancellationException(OrderCancellationException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ExceptionError.ORDER_CANCELLATION, null, ex.getMessageArgs());
    }
}