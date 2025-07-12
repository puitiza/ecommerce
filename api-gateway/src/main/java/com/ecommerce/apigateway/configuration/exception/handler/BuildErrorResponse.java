package com.ecommerce.apigateway.configuration.exception.handler;

import com.ecommerce.apigateway.model.exception.GlobalErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class BuildErrorResponse {

    private static final String TRACE_PARAM = "trace";

    @Value("${configuration.trace:false}")
    private boolean printStackTrace;

    @Value("${configuration.stacktrace-depth:8}")
    private int stackTraceDepth;

    /**
     * Checks if stack trace should be included based on configuration and request parameters.
     *
     * @param exchange the server web exchange
     * @return true if stack trace should be included
     */
    public boolean stackTrace(ServerWebExchange exchange) {
        return printStackTrace && exchange.getRequest().getQueryParams().containsKey(TRACE_PARAM);
    }

    /**
     * Adds stack trace information to the error response.
     *
     * @param errorResponse the error response to modify
     * @param exception     the exception to process
     * @param includeTrace  whether to include detailed stack trace
     */
    public void addTrace(GlobalErrorResponse errorResponse, Exception exception, boolean includeTrace) {
        List<String> stackTrace = new ArrayList<>();
        stackTrace.add(exception.getClass().getCanonicalName() + ": " + exception.getMessage());

        Arrays.stream(exception.getStackTrace())
                .limit(stackTraceDepth / 2)
                .map(String::valueOf)
                .forEach(stackTrace::add);

        errorResponse.setStackTrace(stackTrace);

        if (includeTrace) {
            String detailedTrace = Arrays.stream(exception.getStackTrace())
                    .limit(stackTraceDepth)
                    .map(s -> "   at " + s)
                    .collect(Collectors.joining(System.lineSeparator()));
            errorResponse.setDebugMessage(exception + System.lineSeparator() + detailedTrace);
        }
    }
}
