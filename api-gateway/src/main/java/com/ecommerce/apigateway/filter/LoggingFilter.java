package com.ecommerce.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        log.info("Incoming request requestId:{} method:{} url:{}", request.getId(),request.getMethod().name(), request.getURI());

        return chain.filter(exchange.mutate().build())
                .doOnError(throwable -> log.error("Request failed:", throwable))
                .doOnSuccess(aVoid -> log.info("Response status: {}", response.getStatusCode()));
    }
}


