package com.ecommerce.paymentservice.configuration.exception;

import com.ecommerce.shared.exception.ErrorResponseBuilder;
import com.ecommerce.shared.exception.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionHandlerConfig extends GlobalExceptionHandler {
    public ExceptionHandlerConfig(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

}