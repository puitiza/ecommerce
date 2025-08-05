package com.ecommerce.apigateway.configuration.exception;

import com.ecommerce.apigateway.configuration.exception.handler.RateLimitExceededException;
import com.ecommerce.apigateway.properties.SecurityProperties;
import com.ecommerce.shared.exception.ErrorResponseBuilder;
import com.ecommerce.shared.exception.ExceptionError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j(topic = "GATEWAY_EXCEPTION_HANDLER")
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerConfig {
    private final ErrorResponseBuilder errorResponseBuilder;
    private final ObjectMapper objectMapper;
    private final SecurityProperties securityProperties;

    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class, RateLimitExceededException.class})
    public Mono<Void> handleGlobalException(Exception ex, ServerWebExchange exchange) {
        return switch (ex) {
            case AuthenticationException authEx ->
                    buildErrorResponse(authEx, exchange, ExceptionError.GATEWAY_UNAUTHORIZED, Mono.just(authEx.getMessage()));
            case AccessDeniedException ignored ->
                    buildErrorResponse(ex, exchange, ExceptionError.GATEWAY_FORBIDDEN, buildAccessDeniedDetails(exchange));
            case RateLimitExceededException rateEx ->
                    buildErrorResponse(rateEx, exchange, ExceptionError.GATEWAY_RATE_LIMIT, Mono.just(rateEx.getMessage()));
            default -> buildErrorResponse(ex, exchange, ExceptionError.GATEWAY_UNEXPECTED, Mono.just(ex.getMessage()));
        };
    }

    private Mono<Void> buildErrorResponse(Exception ex, ServerWebExchange exchange, ExceptionError error, Mono<String> detailsMono) {
        return detailsMono.flatMap(details -> {
            log.error("Exception occurred: {} - {}", error.getKey(), ex.getMessage());
            ResponseEntity<Object> responseEntity = errorResponseBuilder.build(ex, exchange, error, null, details);

            exchange.getResponse().setStatusCode(error.getHttpStatus());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(responseEntity.getBody()))
                    .flatMap(bytes -> exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))))
                    .onErrorResume(JsonProcessingException.class, e -> {
                        log.error("Error serializing error response: {}", e.getMessage());
                        return Mono.error(new IllegalStateException("Failed to create error response", e));
                    });
        });
    }

    private Mono<String> buildAccessDeniedDetails(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().name();

        String requiredRole = securityProperties.endpointRoles().values().stream()
                .flatMap(List::stream)
                .filter(e -> path.matches(e.path().replace("/**", ".*")) && e.method().equals(method))
                .map(SecurityProperties.EndpointRole::role)
                .findFirst()
                .map(role -> role.substring(0, 1).toUpperCase() + role.substring(1)) // Capitalize first letter
                .orElse("unknown");

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    Authentication auth = context.getAuthentication();
                    String roles = auth != null && auth.getAuthorities() != null
                            ? auth.getAuthorities().stream()
                            .map(Object::toString)
                            .filter(authName -> authName.startsWith("ROLE_")) //If not included scopes
                            .sorted() // Sort roles alphabetically
                            .collect(Collectors.joining(", "))
                            : "none";
                    return "Access denied to %s (%s). User roles: [%s]. Required role: %s"
                            .formatted(path, method, roles, requiredRole);
                })
                .defaultIfEmpty("Access denied to %s (%s). No authentication available".formatted(path, method));
    }
}