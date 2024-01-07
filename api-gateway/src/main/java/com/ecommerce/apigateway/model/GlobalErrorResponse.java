package com.ecommerce.apigateway.model;

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

}
