package com.ecommerce.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class LoggingFilter implements WebFilter {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .doOnEach(signal -> {
                    if (signal.isOnComplete() || signal.isOnError()) {
                        // Determine if Authorization header should be masked based on active profile
                        var headersToLog = shouldMaskAuthorization()
                                ? maskAuthorizationHeader(exchange.getRequest().getHeaders())
                                : exchange.getRequest().getHeaders();
                        log.info(
                                "Incoming request :: requestId: {}, ip: {}, method: {}, path: {}, headers: {}, response: {}",
                                exchange.getRequest().getId(),
                                exchange.getRequest().getRemoteAddress(),
                                exchange.getRequest().getMethod(),
                                exchange.getRequest().getPath(),
                                headersToLog.entrySet().stream().toList(), // Convert to list for logging
                                exchange.getResponse().getStatusCode()
                        );
                    }
                });
    }

    /**
     * Determines whether the Authorization header should be masked in logs.
     * It will mask if the active profile is NOT "prod".
     * This means it will mask in "dev", "test", etc.
     */
    private boolean shouldMaskAuthorization() {
        return !"prod".equalsIgnoreCase(activeProfile); // Mask only if not dev profile
    }

    /**
     * Creates a new HttpHeaders object with the Authorization header masked.
     * All other headers are copied as-is.
     */
    private HttpHeaders maskAuthorizationHeader(HttpHeaders headers) {
        HttpHeaders maskedHeaders = new HttpHeaders();
        headers.forEach((key, value) -> {
            if ("authorization".equalsIgnoreCase(key)) {
                maskedHeaders.add(key, "Bearer ...");
            } else {
                maskedHeaders.addAll(key, value);
            }
        });
        return maskedHeaders;
    }

}


