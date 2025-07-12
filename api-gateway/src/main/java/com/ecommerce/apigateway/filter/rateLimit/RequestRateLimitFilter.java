package com.ecommerce.apigateway.filter.rateLimit;

import com.ecommerce.apigateway.configuration.exception.handler.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway filter for rate limiting requests based on a key resolver.
 * Uses Redis to enforce rate limits and throws a RateLimitExceededException if the limit is exceeded.
 */
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
            log.info("Applying rate limit for route: {}, path: {}", config.routeId(), exchange.getRequest().getPath());
            return config.keyResolver().resolve(exchange)
                    .flatMap(key -> {
                        log.debug("Rate limit key: {}", key);
                        return rateLimiter.isAllowed(config.routeId(), key);
                    })
                    .flatMap(rateLimitResponse -> {
                        log.debug("Rate limit response: allowed={}, remaining={}",
                                rateLimitResponse.isAllowed(),
                                rateLimitResponse.getHeaders().get("X-RateLimit-Remaining"));
                        if (rateLimitResponse.isAllowed()) {
                            addHeadersToResponse(exchange, rateLimitResponse);
                            return chain.filter(exchange);
                        }
                        return handleTooManyRequests(exchange, rateLimitResponse);
                    });
        };
    }

    /**
     * Adds rate limit headers to the response.
     *
     * @param exchange the server web exchange
     * @param response the rate limiter response
     */
    private void addHeadersToResponse(ServerWebExchange exchange, RateLimiter.Response response) {
        response.getHeaders().forEach(exchange.getResponse().getHeaders()::add);
    }

    /**
     * Handles requests that exceed the rate limit.
     *
     * @param exchange the server web exchange
     * @param response the rate limiter response
     * @return a Mono signaling the error
     */
    private Mono<Void> handleTooManyRequests(ServerWebExchange exchange, RateLimiter.Response response) {
        addHeadersToResponse(exchange, response);
        log.error("Rate limit exceeded for request: {}", exchange.getRequest().getPath());
        return Mono.error(new RateLimitExceededException("You have exceeded the rate limit. Please try later."));
    }

    /**
     * Configuration for the rate limit filter.
     *
     * @param keyResolver the key resolver for rate limiting
     * @param routeId     the ID of the route
     */
    public record Config(KeyResolver keyResolver, String routeId) {
    }
}

