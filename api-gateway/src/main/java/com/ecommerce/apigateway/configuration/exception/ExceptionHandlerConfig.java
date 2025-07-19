package com.ecommerce.apigateway.configuration.exception;

import com.ecommerce.apigateway.configuration.exception.handler.RateLimitExceededException;
import com.ecommerce.shared.exception.ErrorResponseBuilder;
import com.ecommerce.shared.exception.ExceptionError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final ErrorResponseBuilder errorResponseBuilder;
    private final ObjectMapper objectMapper;

    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class, RateLimitExceededException.class})
    public Mono<Void> handleGlobalException(Exception ex, ServerWebExchange exchange) {
        ExceptionError error = switch (ex) {
            case AuthenticationException authEx -> ExceptionError.GATEWAY_UNAUTHORIZED;
            case AccessDeniedException accessEx -> ExceptionError.GATEWAY_FORBIDDEN;
            case RateLimitExceededException rateEx -> ExceptionError.GATEWAY_RATE_LIMIT;
            default -> ExceptionError.GATEWAY_UNEXPECTED;
        };

        log.error("Exception occurred: {} - {}", error.getKey(), ex.getMessage());
        ResponseEntity<Object> responseEntity = errorResponseBuilder.build(ex, exchange, error, null);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(responseEntity.getBody());
            exchange.getResponse().setStatusCode(error.getHttpStatus());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response: {}", e.getMessage());
            return Mono.error(new IllegalStateException("Failed to create error response", e));
        }
    }
}