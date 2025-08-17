package com.ecommerce.shared.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Standard error response for API errors")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "HTTP status code", example = "401")
        int status,
        @Schema(description = "Unique error code", example = "P02")
        String errorCode,
        @Schema(description = "Error message", example = "Unauthorized access. Invalid or missing JWT token.")
        String message,
        @Schema(description = "Additional error details", example = "Invalid token provided")
        String details,
        @Schema(description = "Timestamp of the error", example = "2025-08-01T10:07:34-05:00")
        String timestamp,
        @Schema(description = "Stack trace (optional, only included if trace is enabled)")
        List<String> stackTrace,
        @Schema(description = "Validation errors (if applicable)")
        List<ValidationError> errors) {

    @Schema(description = "Validation error details")
    public record ValidationError(
            @Schema(description = "Field that caused the error", example = "name")
            String field,
            @Schema(description = "Error message for the field", example = "Name cannot be empty")
            String message) {
    }
}