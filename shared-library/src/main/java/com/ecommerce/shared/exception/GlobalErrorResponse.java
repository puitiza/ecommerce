package com.ecommerce.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
public class GlobalErrorResponse {
    private final int status;
    private final String message;
    private String errorCode;
    private String timestamp;
    private List<String> stackTrace;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ValidationError> errors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String debugMessage;

    public GlobalErrorResponse(int status, String message, String errorCode) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode != null ? errorCode : "EC-000";
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private record ValidationError(String field, String message) {
    }

    public void addValidationError(String field, String message) {
        if (Objects.isNull(errors)) {
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message));
    }
}
