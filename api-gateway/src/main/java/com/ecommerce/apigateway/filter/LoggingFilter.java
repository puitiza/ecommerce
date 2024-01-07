package com.ecommerce.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
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
        return chain.filter(exchange)
                .doOnEach(signal -> {
                    if (signal.isOnComplete() || signal.isOnError()) {
                        log.info(
                                "Incoming request :: requestId: {}, ip: {}, method: {},path :{}, headers: {}, response :{}",
                                exchange.getRequest().getId(), exchange.getRequest().getRemoteAddress(),
                                exchange.getRequest().getMethod(), exchange.getRequest().getPath(),
                                exchange.getRequest().getHeaders().entrySet()
                                        .stream().filter(stringListEntry -> !stringListEntry.getKey().equals("Authorization")).toList(),
                                exchange.getResponse().getStatusCode()
                        );
                    }
                });
    }
}


