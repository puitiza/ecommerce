package com.ecommerce.userservice.model.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
public class GlobalErrorResponse {
    private final int status;
    private String timestamp;
    private String errorCode;
    private final String message;
    private String detailMessage = "";
    private ArrayList<String> stackTrace;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ValidationError> errors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String debugMessage;

    private record ValidationError(String field, String message) {
    }

    /**
     * Adds a validation error to the list of errors.
     *
     * @param field   The name of the field that failed validation.
     * @param message The validation error message for the field.
     */
    public void addValidationError(String field, String message) {
        if (Objects.isNull(errors)) {
            errors = new ArrayList<>();
        }
        errors.add(new ValidationError(field, message));
    }
}
