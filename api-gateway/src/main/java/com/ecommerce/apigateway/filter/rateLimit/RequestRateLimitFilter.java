package com.ecommerce.apigateway.filter.rateLimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RequestRateLimitFilter extends AbstractGatewayFilterFactory<RequestRateLimitFilter.Config> {

    private final RateLimiter<RedisRateLimiter.Config> rateLimiter;

    public RequestRateLimitFilter(RateLimiter<RedisRateLimiter.Config> rateLimiter) {
        super(Config.class);
        this.rateLimiter = rateLimiter;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpResponse responseEntity = exchange.getResponse();
            KeyResolver resolver = config.getKeyResolver();
            String routeId = config.getRouteId();

            return resolver.resolve(exchange)
                    .flatMap(key -> rateLimiter.isAllowed(routeId, key))
                    .flatMap(rateLimitResponse -> {
                        if (rateLimitResponse.isAllowed()) {
                            return chain.filter(exchange)
                                    .doOnSuccess(aVoid -> addHeadersToResponse(responseEntity, rateLimitResponse)); // Add headers for 200 responses;
                        } else {
                            return handleTooManyRequests(responseEntity, rateLimitResponse);
                        }
                    });
        };
    }

    private void addHeadersToResponse(ServerHttpResponse responseEntity, RateLimiter.Response response) {
        response.getHeaders().forEach((headerName, headerValue) -> responseEntity.getHeaders().add(headerName, headerValue));
    }

    private Mono<Void> handleTooManyRequests(ServerHttpResponse responseEntity, RateLimiter.Response response) {
        responseEntity.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        responseEntity.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Add rate-limiting headers from RateLimiter.Response
        addHeadersToResponse(responseEntity, response);

        return responseEntity.writeWith(Mono.just(responseEntity.bufferFactory().wrap(
                ("""
                        {"success": false, "message": "You have exceeded the rate limit. Please try again later."}
                        """).getBytes())));
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

