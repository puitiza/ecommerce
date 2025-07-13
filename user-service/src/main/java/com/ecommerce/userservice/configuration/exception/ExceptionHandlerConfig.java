package com.ecommerce.userservice.configuration.exception;

import com.ecommerce.userservice.configuration.exception.handler.BuildErrorResponse;
import com.ecommerce.userservice.configuration.exception.handler.InvalidUserException;
import com.ecommerce.userservice.model.exception.GlobalErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler for the User Service REST API.
 * Catches and processes various exceptions, converting them into standardized
 * `GlobalErrorResponse` objects for consistent API error reporting.
 */
@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerConfig extends ResponseEntityExceptionHandler {

    private final BuildErrorResponse buildErrorResponse;


    /**
     * Handles `MethodArgumentNotValidException` which occurs when `@Valid` annotated
     * request bodies fail validation.
     *
     * @param ex      The MethodArgumentNotValidException instance.
     * @param headers HTTP headers.
     * @param status  The HTTP status code.
     * @param request The current WebRequest.
     * @return A ResponseEntity containing a `GlobalErrorResponse` with validation details.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        log.error("Validation error for request: {}", ex.getMessage(), ex); // Log exception with stack trace
        GlobalErrorResponse errorResponse = new GlobalErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Validation error. Check 'errors' field for details.");
        errorResponse.setErrorCode("P01"); // Custom error code for validation failures.
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        buildErrorResponse.addTrace(errorResponse, ex, buildErrorResponse.stackTrace(request));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handles authentication-related exceptions such as `AuthenticationException`
     * and `InvalidUserException`.
     *
     * @param ex      The caught exception (AuthenticationException or InvalidUserException).
     * @param request The current WebRequest.
     * @return A ResponseEntity containing a `GlobalErrorResponse` with unauthorized status.
     */
    @ExceptionHandler({AuthenticationException.class, InvalidUserException.class})
    public ResponseEntity<Object> handleAuthenticationException(Exception ex, WebRequest request) {
        log.error("Authentication failed or invalid user: {}", ex.getMessage(), ex); // Log exception with stack trace
        GlobalErrorResponse errorResponse = new GlobalErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        errorResponse.setErrorCode("P02"); // Custom error code for authentication failures.
        buildErrorResponse.addTrace(errorResponse, ex, buildErrorResponse.stackTrace(request));

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }


}
