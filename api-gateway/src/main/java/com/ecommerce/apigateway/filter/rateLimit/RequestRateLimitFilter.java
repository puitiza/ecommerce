package com.ecommerce.apigateway.filter.rateLimit;

import com.ecommerce.apigateway.configuration.exception.handler.RateLimitExceededException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.HasRouteId;
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
        return (exchange, chain) -> {
            log.info("Applying rate limit for route: {}, path: {}", config.getRouteId(), exchange.getRequest().getPath());
            return resolver(exchange, config)
                    .flatMap(key -> {
                        log.debug("Rate limit key: {}", key);
                        return rateLimiter.isAllowed(config.getRouteId(), key);
                    })
                    .flatMap(rateLimitResponse -> {
                        log.debug("Rate limit response: allowed={}, remaining={}", rateLimitResponse.isAllowed(), rateLimitResponse.getHeaders().get("X-RateLimit-Remaining"));
                        return rateLimitResponse.isAllowed()
                                ? chain.filter(exchange)
                                .doOnSuccess(aVoid -> addHeadersToResponse(exchange, rateLimitResponse))
                                : handleTooManyRequests(exchange, rateLimitResponse);
                    });
        };
    }

    private Mono<String> resolver(ServerWebExchange exchange, Config config) {
        return config.getKeyResolver().resolve(exchange);
    }

    /**
     * Adds rate limit headers (like X-RateLimit-Remaining) to the response.
     */
    private void addHeadersToResponse(ServerWebExchange exchange, RateLimiter.Response response) {
        response.getHeaders().forEach(exchange.getResponse().getHeaders()::add);
    }

    /**
     * Handles the scenario where the rate limit is exceeded.
     * Adds rate limit headers and returns a Mono.error with a custom exception.
     */
    private Mono<Void> handleTooManyRequests(ServerWebExchange exchange, RateLimiter.Response response) {
        addHeadersToResponse(exchange, response);
        log.error("Rate limit exceeded for request: {}", exchange.getRequest().getPath());
        return Mono.error(new RateLimitExceededException("You have exceeded the rate limit. Please try later."));
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

