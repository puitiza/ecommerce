package com.ecommerce.apigateway.configuration.exception;

import com.ecommerce.apigateway.configuration.exception.handler.RateLimitExceededException;
import com.ecommerce.sharedlibrary.exception.BuildErrorResponse;
import com.ecommerce.sharedlibrary.exception.GlobalErrorResponse;
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

/**
 * Global exception handler for the API Gateway.
 * Handles authentication, access denied, rate limit, and unexpected exceptions.
 */
@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerConfig {

    private final BuildErrorResponse buildErrorResponse;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles global exceptions and returns appropriate error responses.
     *
     * @param ex       the exception to handle
     * @param exchange the server web exchange
     * @return a Mono signaling the error response
     */
    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class, RateLimitExceededException.class})
    public Mono<Void> handleGlobalException(Exception ex, ServerWebExchange exchange) {
        return switch (ex) {
            case AuthenticationException AuthEx ->
                    handleException(AuthEx, exchange, HttpStatus.UNAUTHORIZED, "EC-001", "Authentication failed: " + ex.getMessage());
            case AccessDeniedException AccessEx ->
                    handleException(AccessEx, exchange, HttpStatus.FORBIDDEN, "EC-003", "Access denied: " + ex.getMessage());
            case RateLimitExceededException RateEx ->
                    handleException(RateEx, exchange, HttpStatus.TOO_MANY_REQUESTS, "EC-004", "Rate limit exceeded: " + ex.getMessage());
            default ->
                    handleException(ex, exchange, HttpStatus.INTERNAL_SERVER_ERROR, "EC-005", "Unexpected error: " + ex.getMessage());
        };
    }

    /**
     * Writes an error response to the exchange, including stack trace if configured.
     *
     * @param ex         the exception to include in the response
     * @param exchange   the server web exchange
     * @param statusCode the HTTP status code
     * @param errorCode  the custom error code
     * @param message    the error message
     * @return a Mono signaling the response
     */
    private Mono<Void> handleException(Exception ex, ServerWebExchange exchange, HttpStatus statusCode, String errorCode, String message) {
        log.error("Exception occurred: {} - {}", errorCode, message);

        GlobalErrorResponse errorResponse = new GlobalErrorResponse(statusCode.value(), message, errorCode);
        buildErrorResponse.addTrace(errorResponse, ex, buildErrorResponse.stackTrace(exchange));

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            exchange.getResponse().setStatusCode(statusCode);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response: {}", e.getMessage());
            return Mono.error(new IllegalStateException("Failed to create error response", e));
        }
    }

}
