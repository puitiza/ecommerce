package com.ecommerce.authservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class JwtResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private String token;
    public static final String TYPE = "Bearer";    // Compliant
    private String refreshToken;
}
