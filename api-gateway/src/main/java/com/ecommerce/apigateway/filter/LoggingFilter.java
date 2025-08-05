package com.ecommerce.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Web filter for logging incoming requests and their responses.
 * Logs request details such as ID, IP, method, path, headers, and response status.
 * Masks the Authorization header in non-production environments for security.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class LoggingFilter implements WebFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String MASKED_AUTH_VALUE = "Bearer ...";

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
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
     * Checks if the active profile is production.
     *
     * @return true if the active profile is 'prod', false otherwise
     */
    private boolean shouldMaskAuthorization() {
        return !"prod".equalsIgnoreCase(activeProfile); // Mask only if not dev profile
    }

    /**
     * Masks the Authorization header if present to prevent logging sensitive data.
     *
     * @param headers the original request headers
     * @return a new HttpHeaders instance with the Authorization header masked
     */
    private HttpHeaders maskAuthorizationHeader(HttpHeaders headers) {
        HttpHeaders maskedHeaders = new HttpHeaders();
        headers.forEach((key, value) ->
                maskedHeaders.addAll(key, AUTH_HEADER.equalsIgnoreCase(key) ? List.of(MASKED_AUTH_VALUE) : value));
        return maskedHeaders;
    }

}


