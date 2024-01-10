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
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
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
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                                """
                                {"success": false, "message": "You have exceeded the rate limit. Please try later."}
                                """.getBytes()))
                        .map(dataBuffer -> {
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            addHeadersToResponse(exchange, response); // Call reusable method
                            return dataBuffer;
                        })
        );
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

