package com.ecommerce.userservice.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "'username' field not should be null or empty")
    private String username;

    @NotBlank(message = "'password' field not should be null or empty")
    private String password;
}
