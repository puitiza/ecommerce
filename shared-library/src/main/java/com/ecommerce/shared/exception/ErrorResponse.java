package com.ecommerce.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String message,
        String errorCode,
        String timestamp,
        List<String> stackTrace,
        List<ValidationError> errors,
        String debugMessage) {

    public ErrorResponse(ErrorType errorType, String message, String errorCode) {
        this(
                errorType.getStatus().value(),
                message != null ? message : errorType.getDefaultMessage(),
                errorCode != null ? errorCode : errorType.getDefaultErrorCode(),
                ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                null,
                null,
                null
        );
    }

    public ErrorResponse withValidationError(String field, String message) {
        List<ValidationError> newErrors = new ArrayList<>(Objects.requireNonNullElse(errors, List.of()));
        newErrors.add(new ValidationError(field, message));
        return new ErrorResponse(status, this.message, errorCode, timestamp, stackTrace, newErrors, debugMessage);
    }

    public record ValidationError(String field, String message) {
    }
}
