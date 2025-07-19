package com.ecommerce.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    public record ValidationError(String field, String message) {
    }
}