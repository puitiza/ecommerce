package com.ecommerce.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String errorCode,
        String message,
        String details,
        String timestamp,
        List<String> stackTrace,
        List<ValidationError> errors) {

    public record ValidationError(String field, String message) {
    }
}