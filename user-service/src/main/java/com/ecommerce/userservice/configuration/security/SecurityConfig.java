package com.ecommerce.userservice.configuration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    /**
     * Configures Spring Security with essential settings for authentication, authorization, and exception handling.
     *
     * @param http                     The HttpSecurity configuration object.
     * @param handlerExceptionResolver The HandlerExceptionResolver for handling exceptions in a consistent way.
     * @return The SecurityFilterChain bean, representing the configured security filter chain.
     * @throws Exception If errors occur during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerExceptionResolver handlerExceptionResolver) throws Exception {
        http
                // Disable CORS and CSRF for stateless session management
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)

                // Enforce stateless session management
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define authorization rules for endpoints
                .authorizeHttpRequests(
                        (authz) -> authz
                                .requestMatchers("/user/signup", "/user/login").permitAll()
                                .requestMatchers("/user/v3/api-docs/**", "/user/swagger-ui/**", "/user/swagger-ui.html",
                                        "/favicon.ico").permitAll()
                                .requestMatchers(HttpMethod.GET, "/users/data").hasRole("user")
                                .anyRequest().authenticated())

                // Configure OAuth 2.0 resource server settings
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                        .authenticationEntryPoint(unauthorizedEntryPoint(handlerExceptionResolver))
                        .accessDeniedHandler(accessDeniedHandler(handlerExceptionResolver))
                );

        return http.build();
    }

    /**
     * Creates an AuthenticationEntryPoint bean that delegates to the provided HandlerExceptionResolver
     * for handling authentication-related exceptions, ensuring consistent error response formatting.
     *
     * @param handlerExceptionResolver The HandlerExceptionResolver to delegate to.
     * @return The AuthenticationEntryPoint bean.
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint(HandlerExceptionResolver handlerExceptionResolver) {
        return (request, response, authException) ->
                handlerExceptionResolver.resolveException(request, response, null, authException);
    }

    /**
     * Creates an AccessDeniedHandler bean that delegates to the provided HandlerExceptionResolver
     * for handling access denial exceptions, ensuring consistent error response formatting.
     *
     * @param handlerExceptionResolver The HandlerExceptionResolver to delegate to.
     * @return The AccessDeniedHandler bean.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler(HandlerExceptionResolver handlerExceptionResolver) {
        return (request, response, ex) ->
                handlerExceptionResolver.resolveException(request, response, null, ex);
    }

}
