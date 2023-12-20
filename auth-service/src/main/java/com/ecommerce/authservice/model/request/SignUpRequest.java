package com.ecommerce.authservice.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "'username' field not should be null or empty")
    @Size(min = 3, max = 20, message = "'username' field should be minimum size 3 and maximum 20")
    private String username;

    @NotBlank(message = "'email' field not should be null or empty")
    @Size(min = 5, max = 50, message = "'email' field should be minimum size 5 and maximum 50")
    @Email(message = "email address must be valid")
    private String email;

    @NotBlank(message = "'password' field not should be null or empty")
    @Size(min = 6, max = 40, message = "'password' field should be minimum size 6 and maximum 40")
    private String password;

   // @ValueOfEnum(enumClass = Role.class)
    private String role;
}
