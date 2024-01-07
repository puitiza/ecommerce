package com.ecommerce.apigateway.configuration.exception.handler;

import com.ecommerce.apigateway.model.GlobalErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BuildErrorResponse {
    public static final String TRACE = "trace";

    @Value("${configuration.trace:false}")
    public boolean printStackTrace;

    protected boolean isTraceOn(ServerWebExchange exchange) {
        return exchange.getRequest().getQueryParams().getFirst(TRACE) != null;
    }

    public void addTrace(GlobalErrorResponse errorResponse, Exception exception, boolean trace) {
        if (trace) {
            var depth = (Arrays.stream(exception.getStackTrace()).count() / 8);
            var stackTrace = Arrays.stream(exception.getStackTrace()).limit(depth)
                    .map(String::valueOf)
                    .map(s -> "   at " + s)
                    .collect(Collectors.joining(System.lineSeparator()));
            String message = exception + System.lineSeparator() + stackTrace;
            errorResponse.setDebugMessage(message);
        }

        var stackTrace2 = Arrays.stream(exception.getStackTrace()).limit(4)
                .map(String::valueOf)
                .collect(Collectors.toCollection(ArrayList::new));
        stackTrace2.add(0, exception.getClass().getCanonicalName() + " " + exception.getMessage());
        errorResponse.setStackTrace(stackTrace2);

        var timestamp = ZonedDateTime.now();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss z");
        errorResponse.setTimestamp(timestamp.format(formatter2));

        if (exception instanceof HandledException handledException) {
            errorResponse.setErrorCode(
                    Optional.ofNullable(handledException.getErrorCode())
                            .orElse("EC001")
            );
        }
    }

    public boolean stackTrace(ServerWebExchange request) {
        return (printStackTrace && isTraceOn(request));
    }

}
