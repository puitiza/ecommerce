package com.ecommerce.apigateway.filter;

import com.ecommerce.apigateway.exception.JwtTokenMalformedException;
import com.ecommerce.apigateway.exception.JwtTokenMissingException;
import com.ecommerce.apigateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GatewayFilter {

    private final JwtUtils jwtUtils;
    private final RouterValidator routerValidator;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        log.info("JwtAuthenticationFilter | filter is working");

        var conditionFilter = routerValidator.isSecured.test(request);

        log.info("JwtAuthenticationFilter | filter | isApiSecured.test(request) : " + conditionFilter);

        if (conditionFilter) {
            if (this.isAuthMissing(request)) {
                return this.onError(exchange, HttpStatus.UNAUTHORIZED, "Authentication token is missing or invalid");
            }

            final String token = this.getToken(request);

            try {
                jwtUtils.validateJwtToken(token);
                log.info("Authentication is successful");
            } catch (ExpiredJwtException e) {
                log.info("JwtAuthenticationFilter | filter | ExpiredJwtException | error : " + e.getMessage());
                return this.onError(exchange, HttpStatus.UNAUTHORIZED, "Authentication token has expired");

            } catch (IllegalArgumentException | JwtTokenMalformedException | JwtTokenMissingException
                     | UnsupportedJwtException e) {
                return this.onError(exchange, HttpStatus.BAD_REQUEST, "Invalid authentication token");
            }

            this.updateRequest(exchange, token);
        }
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus, String errorMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Construct a JSON response with error details
        String jsonResponse = """
            {
                "error": "%s",
                "status": "%d"
            }
            """.formatted(errorMessage, httpStatus.value());

        // Write the JSON response to the body
        DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private String getToken(ServerHttpRequest request) {
        final String authorization = request.getHeaders().getOrEmpty("Authorization").get(0);
        final String token = authorization.replace("Bearer ", "");
        log.info("JwtAuthenticationFilter | filter | token : " + token);
        return token;
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    private void updateRequest(ServerWebExchange exchange, String token) {
        Claims claims = jwtUtils.getClaims(token);
        exchange.getRequest().mutate().header("username", String.valueOf(claims.get("username"))).build();
    }

}
