package com.ecommerce.shared.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

    private final MessageSource messageSource;

    public ResponseEntity<Object> build(Exception exception, HttpStatus httpStatus, Object requestContext,
                                        ExceptionError error, Object... messageArgs) {
        return buildInternal(exception, httpStatus, requestContext, error, null, messageArgs);
    }

    public ResponseEntity<Object> buildInternal(Exception exception, HttpStatus httpStatus, Object requestContext,
                                                 ExceptionError error, List<ErrorResponse.ValidationError> validationErrors,
                                                 Object... messageArgs) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(error.getKey() + ".msg", messageArgs, exception.getMessage(), locale);
        String errorCode = messageSource.getMessage(error.getKey() + ".code", null, error.getKey(), locale);

        TraceInfo traceInfo = getTraceInfo(exception, requestContext);

        ErrorResponse errorResponse = new ErrorResponse(
                httpStatus.value(),
                message,
                errorCode,
                ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                traceInfo.stackTraceList(),
                validationErrors,
                traceInfo.debugMessage()
        );

        return ResponseEntity.status(httpStatus).body(errorResponse);
    }

    private TraceInfo getTraceInfo(Exception exception, Object requestContext) {
        if (!shouldIncludeStackTrace(requestContext)) {
            return new TraceInfo(null, null);
        }

        List<String> stackTraceList = Arrays.stream(exception.getStackTrace())
                .limit(stackTraceDepth)
                .map(StackTraceElement::toString)
                .toList();

        String debugMessage = Arrays.stream(exception.getStackTrace())
                .map(s -> "    at " + s)
                .collect(Collectors.joining(System.lineSeparator()));

        return new TraceInfo(stackTraceList, debugMessage);
    }

    private boolean shouldIncludeStackTrace(Object requestContext) {
        if (!printStackTrace) return false;
        return switch (requestContext) {
            case ServerWebExchange exchange -> exchange.getRequest().getQueryParams().containsKey(TRACE_PARAM);
            case WebRequest webRequest -> {
                String[] value = webRequest.getParameterValues(TRACE_PARAM);
                yield value != null && value.length > 0 && value[0].equals("true");
            }
            default -> false;
        };
    }

    private record TraceInfo(List<String> stackTraceList, String debugMessage) {
    }
}