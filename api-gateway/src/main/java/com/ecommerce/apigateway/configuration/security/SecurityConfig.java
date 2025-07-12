package com.ecommerce.apigateway.configuration.security;

import com.ecommerce.apigateway.configuration.exception.ExceptionHandlerConfig;
import com.ecommerce.apigateway.model.properties.PermitUrlsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ExceptionHandlerConfig exceptionHandlerConfig,
                                                            PermitUrlsProperties permitUrls) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for API Gateway (stateless)
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
     * Configures CORS (Cross-Origin Resource Sharing) for the API Gateway.
     * This is essential for allowing Swagger UI (or any frontend) running on a different origin
     * to interact with the API Gateway, especially for OAuth2 flows involving Keycloak.
     * The gateway will invoke the Keycloak token generation, which might be on a different port/domain.
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
        return (exchange, exception) -> exceptionHandlerConfig.handleGlobalException(exception, exchange);
    }

    @Bean
    public ServerAccessDeniedHandler accessDeniedHandler(ExceptionHandlerConfig exceptionHandlerConfig) {
        return (exchange, exception) -> exceptionHandlerConfig.handleGlobalException(exception, exchange);
    }

}
