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

/**
 * Global exception handler providing a centralized way to handle common
 * Spring MVC and custom exceptions. This class serves as the base
 * for specific service exception handlers.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public abstract class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    protected final ErrorResponseBuilder errorResponseBuilder;

    /**
     * Handles validation exceptions from @Valid annotations on method arguments.
     * It formats the response to be clean and easy to consume.
     */
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

    /**
     * Handles type mismatch exceptions for request parameters or path variables.
     */
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

    /**
     * Handles exceptions for missing required request parameters.
     */
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

    /**
     * Catches and handles all custom ServiceExceptions. Subclasses should define their specific
     * ExceptionError, which is then used by the ErrorResponseBuilder.
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Object> handleServiceException(ServiceException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ex.getError(), null, ex.getMessage(), ex.getMessageArgs());
    }

    /**
     * Handles not-found resource exceptions.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ExceptionError.NOT_FOUND, null, ex.getMessage(), ex.getMessageArgs());
    }

    /**
     * The ultimate catch-all for any other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedExceptions(Exception ex, WebRequest request) {
        return errorResponseBuilder.build(ex, request, ExceptionError.INTERNAL_SERVER_ERROR, null, ex.getMessage());
    }
}