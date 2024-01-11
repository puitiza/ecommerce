package com.ecommerce.apigateway.configuration.exception;

import com.ecommerce.apigateway.configuration.exception.handler.BuildErrorResponse;
import com.ecommerce.apigateway.model.exception.GlobalErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerConfig {

    private final BuildErrorResponse buildErrorResponse;

    @ExceptionHandler({AuthenticationException.class})
    public Mono<GlobalErrorResponse> handleAuthenticationException(Exception ex, ServerWebExchange exchange) {
        // Check if response is not already committed
        if (!exchange.getResponse().isCommitted()) {
            log.error("Failed to authenticate the requested element {}", ex.getMessage());
            GlobalErrorResponse errorResponse = new GlobalErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                    ex.getMessage(),"EC-001");
            buildErrorResponse.addTrace(errorResponse, ex, buildErrorResponse.stackTrace(exchange));
            return Mono.just(errorResponse);
        } else { // Avoid further processing if response is already committed
            return Mono.empty();
        }
    }

    @ExceptionHandler({AccessDeniedException.class})
    public Mono<GlobalErrorResponse> handleAccessDeniedException(Exception ex, ServerWebExchange exchange) {
        log.error("Denied to access the requested element {}", ex.getMessage());
        GlobalErrorResponse errorResponse = new GlobalErrorResponse(HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),"EC-003");
        buildErrorResponse.addTrace(errorResponse, ex, buildErrorResponse.stackTrace(exchange)); // Use ServerWebExchange
        return Mono.just(errorResponse);
    }

}
