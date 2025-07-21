package com.ecommerce.apigateway.configuration.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts a JWT into an authentication token with granted authorities.
 * Extracts roles from the JWT's resource_access claim for the specified client.
 */
@Slf4j
@Component
public class JwtAuthConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Value("${spring.security.oauth2.client.registration.ecommerce.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.provider.keycloak.user-name-attribute}")
    private String principalAttribute;

    public JwtAuthConverter() {
        this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(@NonNull Jwt jwt) {
        return Mono.just(jwt)
                .map(this::extractAuthorities)  // Extract authorities
                .map(authorities -> new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt))); // Create AuthenticationToken
    }

    /**
     * Extracts authorities from the JWT, combining standard scopes and client-specific roles.
     *
     * @param jwt the JWT token
     * @return a set of granted authorities
     */
    private Set<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return Stream.concat(jwtGrantedAuthoritiesConverter.convert(jwt).stream(), extractResourceRoles(jwt).stream())
                .collect(Collectors.toSet());
    }

    /**
     * Gets the principal claim name from the JWT.
     *
     * @param jwt the JWT token
     * @return the principal claim name
     */
    private String getPrincipalClaimName(Jwt jwt) {
        return principalAttribute != null ? jwt.getClaim(principalAttribute) : jwt.getClaim(JwtClaimNames.SUB);
    }

    /**
     * Extracts client-specific roles from the JWT's resource_access claim.
     *
     * @param jwt the JWT token
     * @return a set of granted authorities for the client roles
     */
    @SuppressWarnings("unchecked")
    private Set<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess == null || !resourceAccess.containsKey(clientId)) {
            log.debug("No resource_access or clientId {} found in JWT", clientId);
            return Collections.emptySet();
        }

        Object resourceObj = resourceAccess.get(clientId);
        if (!(resourceObj instanceof Map)) {
            log.warn("Invalid resource format for clientId {} in JWT", clientId);
            return Collections.emptySet();
        }

        Map<String, Object> resource = (Map<String, Object>) resourceObj;
        Object rolesObj = resource.getOrDefault("roles", Collections.emptyList());
        if (!(rolesObj instanceof Collection<?> roles)) {
            log.warn("Invalid roles format for clientId {} in JWT", clientId);
            return Collections.emptySet();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(role -> (String) role)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

}
