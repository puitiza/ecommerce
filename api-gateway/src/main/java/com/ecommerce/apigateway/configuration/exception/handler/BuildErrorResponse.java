package com.ecommerce.apigateway.configuration.exception.handler;

import com.ecommerce.apigateway.model.exception.GlobalErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class BuildErrorResponse {
    public static final String TRACE = "trace"; // Query parameter name for enabling trace

    @Value("${configuration.trace:false}")
    public boolean printStackTrace;

    /**
     * Checks if the 'trace' query parameter is present in the request.
     */
    protected boolean isTraceOn(ServerWebExchange exchange) {
        return exchange.getRequest().getQueryParams().getFirst(TRACE) != null;
    }

    /**
     * Populates stack trace information in the GlobalErrorResponse.
     * It includes a simplified stack trace always, and a full debug message (full stack trace)
     * only if globally enabled and explicitly requested by the client.
     *
     * @param errorResponse The error response object to modify.
     * @param exception     The exception that occurred.
     * @param includeFullDebugTrace A boolean indicating if the full debug message (detailed stack trace)
     * should be included based on global config and client request.
     */
    public void addTrace(GlobalErrorResponse errorResponse, Exception exception, boolean includeFullDebugTrace) {
        List<String> simplifiedStackTrace = Arrays.stream(exception.getStackTrace())
                .limit(10) // Limit to first 10 elements for brevity in general response
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());

        // Prepend the exception class and message to the simplified stack trace for immediate context
        List<String> finalSimplifiedStackTrace = simplifiedStackTrace;
        simplifiedStackTrace = Stream.of(exception.getClass().getCanonicalName() + ": " + exception.getMessage() + (simplifiedStackTrace.isEmpty() ? "" : System.lineSeparator()))
                .flatMap(s -> Stream.concat(Stream.of(s), finalSimplifiedStackTrace.stream().map(st -> "   at " + st)))
                .collect(Collectors.toList());

        errorResponse.setStackTrace(simplifiedStackTrace);

        // Include the full stack trace in 'debugMessage' ONLY if 'includeFullDebugTrace' is true
        if (includeFullDebugTrace) {
            String fullStackTrace = Arrays.stream(exception.getStackTrace())
                    .map(StackTraceElement::toString)
                    .map(s -> "   at " + s)
                    .collect(Collectors.joining(System.lineSeparator()));
            String debugMessageContent = exception.getClass().getCanonicalName() + ": " + exception.getMessage() + System.lineSeparator() + fullStackTrace;
            errorResponse.setDebugMessage(debugMessageContent);
        }
    }

    public boolean stackTrace(ServerWebExchange request) {
        return (printStackTrace && isTraceOn(request));
    }

}
