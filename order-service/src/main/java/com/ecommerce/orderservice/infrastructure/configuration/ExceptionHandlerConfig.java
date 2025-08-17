package com.ecommerce.orderservice.infrastructure.configuration;

import com.ecommerce.orderservice.domain.exception.OrderCancellationException;
import com.ecommerce.orderservice.domain.exception.OrderUpdateException;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.shared.application.exception.ErrorResponseBuilder;
import com.ecommerce.shared.exception.GlobalExceptionHandler;
import com.ecommerce.shared.domain.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Service-specific exception handler for the Order Service.
 * It extends the shared GlobalExceptionHandler to centralize common logic
 * while handling specific domain exceptions.
 */
@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionHandlerConfig extends GlobalExceptionHandler {

    public ExceptionHandlerConfig(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    /**
     * Handles all custom domain exceptions that extend ServiceException.
     * This method avoids redundancy by extracting the ExceptionError directly
     * from the exception object itself.
     * The `ex.getError()` method is used to retrieve the correct ExceptionError code.
     */
    @ExceptionHandler({OrderValidationException.class, OrderCancellationException.class, OrderUpdateException.class})
    public ResponseEntity<Object> handleServiceExceptions(ServiceException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ex.getError(), null, ex.getMessage(), ex.getMessageArgs());
    }
}