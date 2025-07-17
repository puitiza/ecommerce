package com.ecommerce.shared.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildErrorResponse {

    private static final String TRACE_PARAM = "trace";

    @Value("${configuration.trace:false}")
    private boolean printStackTrace;

    @Value("${configuration.stacktrace-depth:8}")
    private int stackTraceDepth;

    // Method for WebFlux (API Gateway)
    public boolean shouldIncludeStackTrace(ServerWebExchange exchange) {
        return printStackTrace && exchange.getRequest().getQueryParams().containsKey(TRACE_PARAM);
    }

    // Method for Spring MVC (other services)
    public boolean shouldIncludeStackTrace(WebRequest request) {
        String[] value = request.getParameterValues(TRACE_PARAM);
        return printStackTrace && Objects.nonNull(value) && value.length > 0 && value[0].contentEquals("true");
    }

    /**
     * Adds stack trace information to the error response.
     * This method is overloaded to accommodate both WebFlux and Spring MVC exceptions.
     *
     * @param errorResponse the error response to modify
     * @param exception     the exception to process
     * @param includeTrace  whether to include detailed stack trace
     */
    public void addTrace(GlobalErrorResponse errorResponse, Exception exception, boolean includeTrace) {
        List<String> stackTraceList = new ArrayList<>();
        stackTraceList.add(exception.getClass().getCanonicalName() + ": " + exception.getMessage());

        Arrays.stream(exception.getStackTrace())
                .limit(stackTraceDepth / 2)
                .map(String::valueOf)
                .forEach(stackTraceList::add);
        errorResponse.setStackTrace(stackTraceList);

        if (includeTrace) {
            String detailedTrace = Arrays.stream(exception.getStackTrace())
                    .limit(stackTraceDepth)
                    .map(s -> "    at " + s)
                    .collect(Collectors.joining(System.lineSeparator()));
            errorResponse.setDebugMessage(exception + System.lineSeparator() + detailedTrace);
            log.debug("Added detailed stack trace to error response");
        }
    }

    // Helper method for Spring MVC services to build ResponseEntity
    public ResponseEntity<Object> structure(Exception exception, HttpStatus httpStatus, WebRequest request, String errorCode) {
        GlobalErrorResponse errorResponse = new GlobalErrorResponse(httpStatus.value(), exception.getMessage(), errorCode);
        addTrace(errorResponse, exception, shouldIncludeStackTrace(request));
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
