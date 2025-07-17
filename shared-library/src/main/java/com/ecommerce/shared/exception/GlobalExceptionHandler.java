package com.ecommerce.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public abstract class GlobalExceptionHandler {
    protected final ErrorResponseBuilder errorResponseBuilder;

    protected GlobalExceptionHandler(ErrorResponseBuilder errorResponseBuilder) {
        this.errorResponseBuilder = errorResponseBuilder;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ErrorType.UNPROCESSABLE, null, ErrorCodes.VALIDATION_ERROR);
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorResponse = errorResponse.withValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        errorResponseBuilder.addTrace(errorResponse, ex, errorResponseBuilder.shouldIncludeStackTrace(request));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }
/*
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrorCode());
        errorResponseBuilder.addTrace(errorResponse, ex, errorResponseBuilder.shouldIncludeStackTrace(request));
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }*/
}
