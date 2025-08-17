package com.ecommerce.userservice.configuration.exception;

import com.ecommerce.shared.application.exception.ErrorResponseBuilder;
import com.ecommerce.shared.domain.exception.ExceptionError;
import com.ecommerce.shared.exception.GlobalExceptionHandler;
import com.ecommerce.userservice.configuration.exception.handler.InvalidUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
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

    @ExceptionHandler({AuthenticationException.class, InvalidUserException.class})
    public ResponseEntity<Object> handleInvalidUserException(InvalidUserException ex, WebRequest request) {
        log.error("Invalid user: {}", ex.getMessage(), ex);
        return errorResponseBuilder.build(ex, request, ExceptionError.USER_USERNAME_FOUND, null, ex.getMessage());
    }
}