package com.ecommerce.userservice.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Data Transfer Object (DTO) for user registration requests.
 * Contains user details such as email, username, names, password, and admin status.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")  // Excludes password from toString() for security.
public class UserDto {

    @NotBlank(message = "'email' field not should be null or empty")
    @Size(min = 5, max = 50, message = "'email' field should be minimum size 5 and maximum 50")
    @Email(message = "email address must be valid")
    private String email;

    private String username;
    private String firstname;
    private String lastname;

    @NotBlank(message = "'password' field not should be null or empty")
    @Size(min = 6, max = 40, message = "'password' field should be minimum size 6 and maximum 40")
    private String password;

    @JsonProperty("isAdmin")
    private boolean isAdmin;

}
