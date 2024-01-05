package com.ecommerce.userservice.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
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

    public JwtAuthConverter() {
        this.jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        final Set<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractUserRoles(jwt).stream()
        ).collect(Collectors.toSet());
        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    private String getPrincipalClaimName(Jwt jwt) {
        return jwt.getClaim(clientId);
    }

    private Set<? extends GrantedAuthority> extractUserRoles(Jwt jwt) {
        final Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (Objects.isNull(realmAccess)) {
            return Collections.emptySet();
        }
        Object roles = realmAccess.get("roles");
        if (Objects.isNull(roles) || !Collection.class.isAssignableFrom(roles.getClass())) {
            return Collections.emptySet();
        }

        Collection<?> rolesCollection = (Collection<?>) roles;

        return rolesCollection.stream()
                .filter(String.class::isInstance)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + ((String) role).toUpperCase()))
                .collect(Collectors.toSet());
    }

}
