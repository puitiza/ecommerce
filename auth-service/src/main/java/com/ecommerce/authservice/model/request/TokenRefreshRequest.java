package com.ecommerce.authservice.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@Builder
public class TokenRefreshRequest {

    @NotBlank(message = "'refreshToken' field not should be null or empty")
    private String refreshToken;

}
