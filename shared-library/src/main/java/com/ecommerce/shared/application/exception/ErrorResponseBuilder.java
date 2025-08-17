package com.ecommerce.shared.application.exception;

import com.ecommerce.shared.domain.exception.ExceptionError;
import com.ecommerce.shared.domain.model.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ServerWebExchange;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

    public ResponseEntity<Object> build(Exception exception, Object requestContext, ExceptionError error,
                                        List<ErrorResponse.ValidationError> validationErrors,
                                        String details, Object... messageArgs) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(error.getKey() + ".msg", messageArgs, "Unexpected error", locale);
        String errorCode = messageSource.getMessage(error.getKey() + ".code", null, error.getKey(), locale);

        TraceInfo traceInfo = getTraceInfo(exception, requestContext);

        ErrorResponse errorResponse = new ErrorResponse(
                error.getHttpStatus().value(),
                errorCode,
                message,
                details,
                ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                traceInfo.stackTraceList(),
                validationErrors
        );

        return ResponseEntity.status(error.getHttpStatus()).body(errorResponse);
    }

    private TraceInfo getTraceInfo(Exception exception, Object requestContext) {
        if (!shouldIncludeStackTrace(requestContext)) {
            return new TraceInfo(null);
        }

        List<String> stackTraceList = Arrays.stream(exception.getStackTrace())
                .limit(stackTraceDepth)
                .map(StackTraceElement::toString)
                .toList();
        return new TraceInfo(stackTraceList);
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

    private record TraceInfo(List<String> stackTraceList) {
    }
}