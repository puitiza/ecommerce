package com.ecommerce.apigateway.model.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class GlobalErrorResponse {
    private final int status;
    private final String timestamp;
    private final String errorCode;
    private final String message;
    private List<String> stackTrace;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ValidationError> errors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String debugMessage;

    public GlobalErrorResponse(int status, String message, String errorCode) {
        this.status = status;
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.errorCode = errorCode != null ? errorCode : "EC-000";
        this.message = message;
    }

    private record ValidationError(String field, String message) {
    }

}
