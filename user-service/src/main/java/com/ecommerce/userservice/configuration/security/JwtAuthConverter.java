package com.ecommerce.userservice.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Value("${keycloak.resource}")
    public String clientId;

    @Value("${keycloak.principal-attribute}")
    public String principalAttribute;

    public JwtAuthConverter() {
        this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        final Set<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractResourceRoles(jwt,clientId).stream()
        ).collect(Collectors.toSet());
        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    private String getPrincipalClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        if (principalAttribute != null) {
            claimName = principalAttribute;
        }
        return jwt.getClaim(claimName);
    }

    private Set<? extends GrantedAuthority> extractResourceRoles(Jwt jwt, String clientId) {
        Map<?, ?> resourceAccess = jwt.getClaim("resource_access"); // Use raw Map for initial access
        if (resourceAccess == null) {
            return Collections.emptySet();
        }

        // Safely extract the resource object
        Map<?, ?> resource = Optional.ofNullable(resourceAccess.get(clientId))
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .orElse(Collections.emptyMap());

        // Safely extract the roles collection
        Collection<?> resourceRoles = Optional.ofNullable(resource.get("roles"))
                .filter(Collection.class::isInstance)
                .map(Collection.class::cast)
                .orElse(Collections.emptyList());

        // Filter and map to GrantedAuthority, ensuring String elements
        return resourceRoles.stream()
                .filter(String.class::isInstance)
                .map(role -> (String) role)  // Explicit cast to String
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

}
