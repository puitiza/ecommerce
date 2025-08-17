package com.ecommerce.shared.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public abstract class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    protected final ErrorResponseBuilder errorResponseBuilder;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorResponse.ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        String message;
        String details;

        if (validationErrors.size() > 1) {
            message = "Validation failed for multiple fields.";
            details = "Multiple validation errors occurred. Please check the 'errors' field for details on each validation failure.";
        } else {
            ErrorResponse.ValidationError firstError = validationErrors.getFirst();
            message = firstError.message();
            details = String.format(
                    "Validation failed for object '%s' on field '%s': rejected value [%s]; default message [%s]",
                    ex.getObjectName(),
                    firstError.field(),
                    ex.getBindingResult().getFieldValue(firstError.field()),
                    firstError.message()
            );
        }

        return errorResponseBuilder.build(ex, request, ExceptionError.VALIDATION_ERROR, validationErrors, details, message);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            @NonNull TypeMismatchException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        String errorDetail = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getPropertyName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return errorResponseBuilder.build(ex, request, ExceptionError.VALIDATION_ERROR, null, ex.getMessage(), errorDetail);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        String errorDetail = String.format("Missing required parameter '%s' of type '%s'",
                ex.getParameterName(), ex.getParameterType());
        return errorResponseBuilder.build(ex, request, ExceptionError.VALIDATION_ERROR, null, ex.getMessage(), errorDetail);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ex.getError(), null, ex.getMessage(), ex.getMessageArgs());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ExceptionError.NOT_FOUND, null, ex.getMessage(), ex.getMessageArgs());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedExceptions(Exception ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ExceptionError.INTERNAL_SERVER_ERROR, null, ex.getMessage());
    }
}