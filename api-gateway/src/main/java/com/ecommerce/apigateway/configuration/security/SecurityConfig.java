package com.ecommerce.apigateway.configuration.security;

import com.ecommerce.apigateway.configuration.exception.ExceptionHandlerConfig;
import com.ecommerce.apigateway.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for the API Gateway.
 * Configures OAuth2 resource server with JWT, public endpoints, and CORS.
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;
    private final SecurityProperties securityProperties;

    /**
     * Configures the security filter chain for the API Gateway.
     * Allows public access to Swagger and user signup/login endpoints, requires authentication for others.
     *
     * @param http                   the ServerHttpSecurity instance
     * @param exceptionHandlerConfig the exception handler configuration
     * @return the configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ExceptionHandlerConfig exceptionHandlerConfig) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF for API Gateway (stateless)
                .authorizeExchange(authz -> {
                    // Configure permit-all URLs dynamically
                    securityProperties.permitUrls().forEach((service, urls) ->
                            urls.forEach(url ->
                                    url.methods().forEach(method ->
                                            authz.pathMatchers(HttpMethod.valueOf(method), url.path()).permitAll()
                                    )
                            )
                    );
                    // Configure role-based endpoints
                    securityProperties.endpointRoles().forEach((service, endpoints) ->
                            endpoints.forEach(endpoint ->
                                    authz.pathMatchers(HttpMethod.valueOf(endpoint.method()), endpoint.path())
                                            .hasRole(endpoint.role())
                            )
                    );
                    authz.anyExchange().authenticated(); // Require authentication for all other requests
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                        .authenticationEntryPoint((exchange, exception) -> exceptionHandlerConfig.handleGlobalException(exception, exchange))
                        .accessDeniedHandler((exchange, exception) -> exceptionHandlerConfig.handleGlobalException(exception, exchange))
                )
                .build();
    }

}
