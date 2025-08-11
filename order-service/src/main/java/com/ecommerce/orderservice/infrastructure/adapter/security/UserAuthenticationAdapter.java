package com.ecommerce.orderservice.infrastructure.adapter.security;

import com.ecommerce.orderservice.application.dto.UserAuthenticationDetails;
import com.ecommerce.orderservice.application.port.out.UserAuthenticationPort;
import lombok.RequiredArgsConstructor;
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