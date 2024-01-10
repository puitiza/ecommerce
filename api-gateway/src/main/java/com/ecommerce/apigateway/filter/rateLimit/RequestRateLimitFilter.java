package com.ecommerce.apigateway.filter.rateLimit;

import com.ecommerce.apigateway.configuration.exception.handler.RateLimitExceededException;
import com.ecommerce.apigateway.model.exception.GlobalErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RequestRateLimitFilter extends AbstractGatewayFilterFactory<RequestRateLimitFilter.Config> {

    private final RateLimiter<RedisRateLimiter.Config> rateLimiter;

    public RequestRateLimitFilter(RateLimiter<RedisRateLimiter.Config> rateLimiter) {
        super(Config.class);
        this.rateLimiter = rateLimiter;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> resolver(exchange, config)
                .flatMap(key -> rateLimiter.isAllowed(config.getRouteId(), key))
                .flatMap(rateLimitResponse -> rateLimitResponse.isAllowed()
                        ? chain.filter(exchange)
                        .doOnSuccess(aVoid -> addHeadersToResponse(exchange, rateLimitResponse))
                        : handleTooManyRequests(exchange, rateLimitResponse));
    }

    private Mono<String> resolver(ServerWebExchange exchange, Config config) {
        return config.getKeyResolver().resolve(exchange);
    }

    private void addHeadersToResponse(ServerWebExchange exchange, RateLimiter.Response response) {
        response.getHeaders().forEach(exchange.getResponse().getHeaders()::add); // Enhanced using :: operator
    }

    private Mono<Void> handleTooManyRequests(ServerWebExchange exchange, RateLimiter.Response response) {
        GlobalErrorResponse errorResponse = new GlobalErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "You have exceeded the rate limit. Please try later.",
                "EC-002"
        );
        ObjectMapper mapper = new ObjectMapper(); // Assuming Jackson is available
        String jsonResponse = null;
        try {
            jsonResponse = mapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize GlobalErrorResponse to JSON: {}", e.getMessage());
        }
        // Return the JSON response with appropriate headers
        if (jsonResponse != null) {
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(jsonResponse.getBytes()))
                            .map(dataBuffer -> {
                                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                addHeadersToResponse(exchange, response); // Call reusable method
                                return dataBuffer;
                            })
            );
        } else {
            // Handle the case where JSON serialization failed
            return Mono.error(new IllegalStateException("Failed to create 429 error response"));
        }
    }


    public static class Config implements HasRouteId {

        @Getter
        private final KeyResolver keyResolver;

        @Setter
        private String routeId;

        public Config(KeyResolver keyResolver) {
            this.keyResolver = keyResolver;
        }

        @Override
        public String getRouteId() {
            return routeId;
        }

    }
}

