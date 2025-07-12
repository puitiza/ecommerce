package com.ecommerce.apigateway.configuration.exception;

import com.ecommerce.apigateway.configuration.exception.handler.BuildErrorResponse;
import com.ecommerce.apigateway.configuration.exception.handler.RateLimitExceededException;
import com.ecommerce.apigateway.model.exception.GlobalErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Centralized exception handler for various security and custom exceptions.
     * Dispatches to specific handlers based on exception type.
     */
    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class, RateLimitExceededException.class})
    public Mono<Void> handleGlobalException(Exception ex, ServerWebExchange exchange) {
        return switch (ex) {
            case AuthenticationException authenticationException ->
                    handleAuthenticationException(authenticationException, exchange);
            case AccessDeniedException accessDeniedException ->
                    handleAccessDeniedException(accessDeniedException, exchange);
            case RateLimitExceededException rateLimitExceededException ->
                    handleTooManyRequestException(rateLimitExceededException, exchange);
            default -> handleUnexpectedException(ex, exchange); // Handle any unexpected exceptions
        };
    }

    private Mono<Void> handleAuthenticationException(AuthenticationException ex, ServerWebExchange exchange) {
        log.error("Failed to authenticate the requested element {}", ex.getMessage());
        return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "EC-001", ex);
    }

    private Mono<Void> handleAccessDeniedException(AccessDeniedException ex, ServerWebExchange exchange) {
        log.error("Denied to access the requested element {}", ex.getMessage());
        return writeErrorResponse(exchange, HttpStatus.FORBIDDEN, "EC-003", ex);
    }

    private Mono<Void> handleTooManyRequestException(RateLimitExceededException ex, ServerWebExchange exchange) {
        log.error("Number of Requests has exceeded the limit: {}", ex.getMessage());
        return writeErrorResponse(exchange, HttpStatus.TOO_MANY_REQUESTS, "EC-004", ex);
    }

    private Mono<Void> handleUnexpectedException(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected exception occurred: {}", ex.getMessage());
        return writeErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "EC-005", ex);
    }

    /**
     * Writes the error response to the ServerWebExchange.
     * Sets the status code, content type, and writes the JSON error body.
     */
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus statusCode, String errorCode, Exception ex) {

        GlobalErrorResponse errorResponse = new GlobalErrorResponse(statusCode.value(), ex.getMessage(), errorCode);
        buildErrorResponse.addTrace(errorResponse, ex, buildErrorResponse.stackTrace(exchange));

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response: {}", e.getMessage());
            return Mono.error(new IllegalStateException("Failed to create error response due to serialization error.", e));
        }

        // Set response status and headers, then write the response body, Use Mono.defer for lazy subscription
        return Mono.defer(() -> {
            exchange.getResponse().setStatusCode(statusCode);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        });
    }

}
