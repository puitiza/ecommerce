package com.ecommerce.apigateway.configuration.security;

import com.ecommerce.apigateway.configuration.exception.ExceptionHandlerConfig;
import com.ecommerce.apigateway.model.properties.PermitUrlsProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ExceptionHandlerConfig exceptionHandlerConfig,
                                                            PermitUrlsProperties permitUrls) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(
                        (authz) -> authz
                                .pathMatchers(HttpMethod.GET, permitUrls.getSwagger()).permitAll()
                                .pathMatchers(permitUrls.getUsers()).permitAll()
                                .pathMatchers(HttpMethod.GET, "/users/data").hasRole("user")
                                .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                        .authenticationEntryPoint(unauthorizedEntryPoint(exceptionHandlerConfig))
                        .accessDeniedHandler(accessDeniedHandler(exceptionHandlerConfig))
                )
                .build();
    }

    /**
     * This filter enables CORS (Cross-Origin Resource Sharing) to allow Swagger UI on localhost:9090 to access resources on the API gateway.
     * The gateway will invoke the keycloak token generation,
     * which runs on <a href="http://localhost:9090">localhost:9090</a>.
     *
     * @return CorsWebFilter
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        final CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Collections.singletonList("http://localhost:9090"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "HEAD", "PUT", "DELETE"));
        corsConfig.addAllowedHeader("Access-Control-Allow-Origin");

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    @Bean
    public ServerAuthenticationEntryPoint unauthorizedEntryPoint(ExceptionHandlerConfig exceptionHandlerConfig) {
        return (exchange, exception) -> exceptionHandlerConfig.handleAuthenticationException(exception, exchange)
                .flatMap(errorResponse -> {
                    byte[] bytes;
                    try {
                        bytes = objectMapper.writeValueAsBytes(errorResponse);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return response.writeWith(Mono.just(new DefaultDataBufferFactory().wrap(bytes)));
                });
    }

    @Bean
    public ServerAccessDeniedHandler accessDeniedHandler(ExceptionHandlerConfig exceptionHandlerConfig) {
        return (exchange, exception) -> exceptionHandlerConfig.handleAccessDeniedException(exception, exchange)
                .flatMap(errorResponse -> {
                    byte[] bytes;
                    try {
                        bytes = objectMapper.writeValueAsBytes(errorResponse);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return response.writeWith(Mono.just(new DefaultDataBufferFactory().wrap(bytes)));
                });
    }


}
