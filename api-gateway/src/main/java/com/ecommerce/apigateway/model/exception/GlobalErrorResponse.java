package com.ecommerce.apigateway.model.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class GlobalErrorResponse {
    private final int status;
    private String timestamp;
    private String errorCode = "EC-xxx";
    private final String message;
    private String detailMessage = "";
    private ArrayList<String> stackTrace;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ValidationError> errors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String debugMessage;

    public GlobalErrorResponse(int status, String message,String errorCode) {
        this.status = status;
        this.timestamp = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss z").format(ZonedDateTime.now());
        this.errorCode = errorCode;
        this.message = message;
    }

    private record ValidationError(String field, String message) {
    }

}
