package com.ecommerce.apigateway.filter.rateLimit;

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
            ServerHttpResponse response = exchange.getResponse();
            KeyResolver resolver = config.keyResolver;
            String routeId = config.getRouteId();

            return resolver.resolve(exchange)
                    .flatMap(key -> rateLimiter.isAllowed(routeId, key))
                    .flatMap(rateLimitResponse -> {
                        if (rateLimitResponse.isAllowed()) {
                            return chain.filter(exchange);
                        } else {
                            return responseTooManyRequests(response);
                        }
                    });
        };
    }

    private Mono<Void> responseTooManyRequests(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(
                ("""
                        {"success": false, "message": "You have exceeded the rate limit. Please try again in"}
                        """).getBytes())));
    }

    public static class Config implements HasRouteId {

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

