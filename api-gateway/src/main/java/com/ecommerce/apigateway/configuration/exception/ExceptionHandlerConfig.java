package com.ecommerce.apigateway.configuration.exception;

import com.ecommerce.apigateway.configuration.exception.handler.RateLimitExceededException;
import com.ecommerce.shared.exception.ErrorCodes;
import com.ecommerce.shared.exception.ErrorResponse;
import com.ecommerce.shared.exception.ErrorResponseBuilder;
import com.ecommerce.shared.exception.ErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final ErrorResponseBuilder errorResponseBuilder;
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
            case AuthenticationException authEx ->
                    handleException(authEx, exchange, ErrorType.UNAUTHORIZED, ErrorCodes.GATEWAY_UNAUTHORIZED, "Authentication failed: " + ex.getMessage());
            case AccessDeniedException accessEx ->
                    handleException(accessEx, exchange, ErrorType.FORBIDDEN, ErrorCodes.GATEWAY_FORBIDDEN, "Access denied: " + ex.getMessage());
            case RateLimitExceededException rateEx ->
                    handleException(rateEx, exchange, ErrorType.RATE_LIMIT_EXCEEDED, ErrorCodes.GATEWAY_RATE_LIMIT, "Rate limit exceeded: " + ex.getMessage());
            default ->
                    handleException(ex, exchange, ErrorType.NOT_FOUND, ErrorCodes.GATEWAY_UNEXPECTED, "Unexpected error: " + ex.getMessage());
        };
    }

    private Mono<Void> handleException(Exception ex, ServerWebExchange exchange, ErrorType errorType, String errorCode, String message) {
        log.error("Exception occurred: {} - {}", errorCode, message);
        ErrorResponse errorResponse = errorResponseBuilder.structure(ex, errorType, message, errorCode);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            exchange.getResponse().setStatusCode(errorType.getStatus());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response: {}", e.getMessage());
            return Mono.error(new IllegalStateException("Failed to create error response", e));
        }
    }

}
