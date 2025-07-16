package com.ecommerce.sharedlibrary.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor // Added for Jackson deserialization if needed, or if you use a builder pattern
@AllArgsConstructor // Added for the constructor with all final fields
public class GlobalErrorResponse {
    private int status;
    private String timestamp;
    private String errorCode;
    private String message;
    private String detailMessage = ""; // From order-service/payment-service GlobalErrorResponse
    private List<String> stackTrace;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ValidationError> errors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String debugMessage;

    // Common constructor from various services
    public GlobalErrorResponse(int status, String message, String errorCode) {
        this.status = status;
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.errorCode = errorCode != null ? errorCode : "EC-000";
        this.message = message;
    }

    // Constructor from order-service/payment-service with timestamp logic ----------------------------------
    public GlobalErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss z"));
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
