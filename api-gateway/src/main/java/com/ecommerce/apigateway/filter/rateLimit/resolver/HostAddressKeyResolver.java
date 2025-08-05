package com.ecommerce.apigateway.filter.rateLimit.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * Key resolver for rate limiting based on the client's IP address.
 * Can be extended to use other keys (e.g., user ID from JWT claims) for more granular rate limiting.
 * <p>
 * Example: Use SecurityContextHolder to extract user details for authenticated requests.
 */
@Slf4j
@Component
public class HostAddressKeyResolver implements KeyResolver {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                .map(InetSocketAddress::getHostString)
                .doOnNext(ip -> log.debug("Resolved rate limit key by IP: {}", ip))
                .switchIfEmpty(Mono.error(new IllegalStateException("Remote address is null")));
    }

}
