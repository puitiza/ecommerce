package com.ecommerce.authservice.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
    public static final String TOKEN_TYPE = "Bearer";
}
