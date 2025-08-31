package com.ecommerce.apigateway.filter.rateLimit.resolver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;


@Slf4j
@Primary
@Component
public class UserOrIpKeyResolver implements KeyResolver {
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth != null && auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal())
                .map(Jwt::getSubject)
                .doOnNext(userId -> log.debug("Rate limit key resolved by user ID: {}", userId))
                .switchIfEmpty(resolveByIp(exchange));
    }

    private Mono<String> resolveByIp(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                .map(InetSocketAddress::getHostString)
                .doOnNext(ip -> log.debug("Rate limit key resolved by IP: {}", ip))
                .switchIfEmpty(Mono.error(new IllegalStateException("Remote address is null")));
    }
}
