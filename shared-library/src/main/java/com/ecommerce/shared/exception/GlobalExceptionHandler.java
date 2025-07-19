package com.ecommerce.shared.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
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

import java.util.List;

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
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorResponse.ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();
        return errorResponseBuilder.buildInternal(
                ex,
                HttpStatus.UNPROCESSABLE_ENTITY,
                request,
                ExceptionError.VALIDATION_ERROR,
                validationErrors
        );
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, mapErrorToHttpStatus(ex.getError()), request, ex.getError(), ex.getMessageArgs());
    }

    private HttpStatus mapErrorToHttpStatus(ExceptionError error) {
        return switch (error) {
            case VALIDATION_ERROR -> HttpStatus.UNPROCESSABLE_ENTITY;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case UNAUTHORIZED, GATEWAY_UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, GATEWAY_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case RATE_LIMIT_EXCEEDED, GATEWAY_RATE_LIMIT -> HttpStatus.TOO_MANY_REQUESTS;
            case ORDER_VALIDATION, ORDER_CANCELLATION, PRODUCT_INVALID_INVENTORY,
                 USER_USERNAME_FOUND, USER_EMAIL_FOUND, USER_ROLE_NOT_FOUND -> HttpStatus.BAD_REQUEST;
            case PRODUCT_UPDATE_FAILED, INTERNAL_SERVER_ERROR, GATEWAY_UNEXPECTED -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}