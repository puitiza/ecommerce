package com.ecommerce.userservice.configuration.exception;

import com.ecommerce.shared.exception.ErrorCodes;
import com.ecommerce.shared.exception.ErrorResponseBuilder;
import com.ecommerce.shared.exception.GlobalExceptionHandler;
import com.ecommerce.userservice.configuration.exception.handler.InvalidUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for the User Service REST API.
 * Catches and processes various exceptions, converting them into standardized
 * `GlobalErrorResponse` objects for consistent API error reporting.
 */
@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ExceptionHandlerConfig extends GlobalExceptionHandler {

    public ExceptionHandlerConfig(ErrorResponseBuilder errorResponseBuilder) {
        super(errorResponseBuilder);
    }

    /**
     * Handles authentication-related exceptions such as 'AuthenticationException'
     * and 'InvalidUserException'.
     *
     * @param ex      The caught exception (AuthenticationException or InvalidUserException).
     * @param request The current WebRequest.
     * @return A ResponseEntity containing a `GlobalErrorResponse` with unauthorized status.
     */
    @ExceptionHandler({AuthenticationException.class, InvalidUserException.class})
    public ResponseEntity<Object> handleAuthenticationException(Exception ex, WebRequest request) {
        log.error("Authentication failed or invalid user: {}", ex.getMessage(), ex);
        return errorResponseBuilder.structure(ex, HttpStatus.UNAUTHORIZED, request, ErrorCodes.USER_INVENTORY);
    }


}
