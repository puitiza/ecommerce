package com.ecommerce.shared.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
public abstract class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    protected final ErrorResponseBuilder errorResponseBuilder;
    protected final MessageSource messageSource;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        ErrorResponse errorResponse = errorResponseBuilder.createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ExceptionError.VALIDATION_ERROR, null);
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorResponse = errorResponse.withValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(status)
                .body(errorResponseBuilder.addTrace(errorResponse, ex, errorResponseBuilder.shouldIncludeStackTrace(request)));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException ex, WebRequest request) {
        String message = messageSource.getMessage(
                ex.getError().getKey() + ".msg",
                ex.getMessageArgs(),
                ex.getMessage(),
                LocaleContextHolder.getLocale()
        );
        ErrorResponse errorResponse = errorResponseBuilder.createErrorResponse(
                mapErrorToHttpStatus(ex.getError()), ex.getError(), message, ex.getMessageArgs()
        );
        return ResponseEntity.status(mapErrorToHttpStatus(ex.getError()))
                .body(errorResponseBuilder.addTrace(errorResponse, ex, errorResponseBuilder.shouldIncludeStackTrace(request)));
    }

    private HttpStatus mapErrorToHttpStatus(ExceptionError error) {
        return switch (error) {
            case VALIDATION_ERROR -> HttpStatus.UNPROCESSABLE_ENTITY;
            case NOT_FOUND, ORDER_NOT_FOUND, PRODUCT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNAUTHORIZED, GATEWAY_UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, GATEWAY_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case RATE_LIMIT_EXCEEDED, GATEWAY_RATE_LIMIT -> HttpStatus.TOO_MANY_REQUESTS;
            case ORDER_VALIDATION, ORDER_CANCELLATION, PRODUCT_INVALID_INVENTORY,
                 USER_USERNAME_FOUND, USER_EMAIL_FOUND, USER_ROLE_NOT_FOUND -> HttpStatus.BAD_REQUEST;
            case PRODUCT_UPDATE_FAILED, INTERNAL_SERVER_ERROR, GATEWAY_UNEXPECTED -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}