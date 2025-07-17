package com.ecommerce.shared.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorResponseBuilder {

    private static final String TRACE_PARAM = "trace";

    @Value("${configuration.trace:false}")
    private boolean printStackTrace;

    @Value("${configuration.stacktrace-depth:8}")
    private int stackTraceDepth;

    public boolean shouldIncludeStackTrace(Object request) {
        if (!printStackTrace) return false;
        return switch (request) {
            case ServerWebExchange exchange -> exchange.getRequest().getQueryParams().containsKey(TRACE_PARAM);
            case WebRequest webRequest -> {
                String[] value = webRequest.getParameterValues(TRACE_PARAM);
                yield value != null && value.length > 0 && value[0].equals("true");
            }
            default -> false;
        };
    }

    public ErrorResponse addTrace(ErrorResponse errorResponse, Exception exception, boolean includeTrace) {
        List<String> stackTraceList = Arrays.stream(exception.getStackTrace())
                .limit(stackTraceDepth)
                .map(StackTraceElement::toString)
                .toList();

        return new ErrorResponse(
                errorResponse.status(),
                errorResponse.message(),
                errorResponse.errorCode(),
                errorResponse.timestamp(),
                stackTraceList,
                errorResponse.errors(),
                includeTrace ? Arrays.stream(exception.getStackTrace())
                        //.limit(stackTraceDepth)
                        .map(s -> "    at " + s)
                        .collect(Collectors.joining(System.lineSeparator())) : null
        );
    }

    public ResponseEntity<Object> structure(Exception exception, HttpStatus httpStatus, WebRequest request, String errorCode) {
        ErrorType errorType = mapHttpStatusToErrorType(httpStatus);
        ErrorResponse errorResponse = errorType.create(exception.getMessage(), errorCode);
        return ResponseEntity.status(httpStatus).body(addTrace(errorResponse, exception, shouldIncludeStackTrace(request)));
    }

    public ErrorResponse structure(Exception exception, ErrorType errorType, String message, String errorCode) {
        ErrorResponse errorResponse = new ErrorResponse(errorType, message, errorCode);
        return addTrace(errorResponse, exception, false);
    }

    private ErrorType mapHttpStatusToErrorType(HttpStatus httpStatus) {
        return switch (httpStatus) {
            case UNPROCESSABLE_ENTITY -> ErrorType.UNPROCESSABLE;
            case NOT_FOUND -> ErrorType.NOT_FOUND;
            case UNAUTHORIZED -> ErrorType.UNAUTHORIZED;
            case FORBIDDEN -> ErrorType.FORBIDDEN;
            case TOO_MANY_REQUESTS -> ErrorType.RATE_LIMIT_EXCEEDED;
            default -> ErrorType.INTERNAL_ERROR;
        };
    }
}
