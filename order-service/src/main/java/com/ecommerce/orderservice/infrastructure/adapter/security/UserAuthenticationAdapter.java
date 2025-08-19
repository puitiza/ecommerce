package com.ecommerce.orderservice.infrastructure.adapter.security;

import com.ecommerce.orderservice.domain.port.out.UserAuthenticationPort;
import com.ecommerce.orderservice.infrastructure.adapter.security.dto.UserAuthenticationDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticationAdapter implements UserAuthenticationPort {

    @Override
    public UserAuthenticationDetails getUserDetails() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt) {
            String token = "Bearer " + jwt.getToken().getTokenValue();
            String userId = jwt.getName();
            return new UserAuthenticationDetails(token, userId);
        }
        throw new SecurityException("No JWT authentication token found in security context");
    }
}