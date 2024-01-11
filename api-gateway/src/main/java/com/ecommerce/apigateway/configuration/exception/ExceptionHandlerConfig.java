package com.ecommerce.apigateway.configuration.exception;

import com.ecommerce.apigateway.configuration.exception.handler.BuildErrorResponse;
import com.ecommerce.apigateway.configuration.exception.handler.RateLimitExceededException;
import com.ecommerce.apigateway.model.exception.GlobalErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

    @ExceptionHandler({AuthenticationException.class})
    public Mono<Void> handleAuthenticationException(Exception ex, ServerWebExchange exchange) {
        log.error("Failed to authenticate the requested element {}", ex.getMessage());
        GlobalErrorResponse errorResponse = new GlobalErrorResponse(HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(), "EC-001");
        buildErrorResponse.addTrace(errorResponse, ex, buildErrorResponse.stackTrace(exchange));
        return writeResponse(exchange, errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public Mono<Void> handleAccessDeniedException(Exception ex, ServerWebExchange exchange) {
        log.error("Denied to access the requested element {}", ex.getMessage());
        GlobalErrorResponse errorResponse = new GlobalErrorResponse(HttpStatus.FORBIDDEN.value(),
                ex.getMessage(), "EC-003");
        buildErrorResponse.addTrace(errorResponse, ex, buildErrorResponse.stackTrace(exchange));
        return writeResponse(exchange, errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({RateLimitExceededException.class})
    public Mono<Void> handleTooManyRequestException(Exception ex, ServerWebExchange exchange) {
        log.error("Number of Requests has exceeded the limit: {}", ex.getMessage());
        GlobalErrorResponse errorResponse = new GlobalErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(),
                "You have exceeded the rate limit. Please try later.", "EC-004");
        buildErrorResponse.addTrace(errorResponse, ex, buildErrorResponse.stackTrace(exchange));
        return writeResponse(exchange, errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, GlobalErrorResponse errorResponse, HttpStatusCode statusCode) {
        byte[] bytes;
        var objectMapper = new ObjectMapper();
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            return Mono.error(new IllegalStateException("Failed to create error response"));
        }

        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
                        .map(dataBuffer -> {
                            exchange.getResponse().setStatusCode(statusCode);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            return dataBuffer;
                        })
        );
    }

}
