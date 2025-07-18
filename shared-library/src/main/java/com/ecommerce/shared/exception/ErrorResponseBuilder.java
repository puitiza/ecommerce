package com.ecommerce.shared.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;

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
                        .map(s -> "    at " + s)
                        .collect(Collectors.joining(System.lineSeparator())) : null
        );
    }

    public ErrorResponse structure(Exception exception, HttpStatus httpStatus, ExceptionError error, String customMessage, Object... messageArgs) {
        ErrorResponse errorResponse = createErrorResponse(httpStatus, error, customMessage, messageArgs);
        return addTrace(errorResponse, exception, false);
    }

    public ErrorResponse createErrorResponse(HttpStatus httpStatus, ExceptionError error, String customMessage, Object... messageArgs) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = customMessage != null ? customMessage :
                messageSource.getMessage(error.getKey() + ".msg", messageArgs, "Unknown error", locale);
        String errorCode = messageSource.getMessage(error.getKey() + ".code", null, error.getKey(), locale);
        return new ErrorResponse(httpStatus, message, errorCode);
    }
}