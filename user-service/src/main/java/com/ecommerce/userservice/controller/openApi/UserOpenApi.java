package com.ecommerce.userservice.controller.openApi;

import com.ecommerce.userservice.model.request.LoginRequest;
import com.ecommerce.userservice.model.request.UserDto;
import com.ecommerce.userservice.model.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import static com.ecommerce.userservice.model.templateAPI.AuthResponseTemplate.UNAUTHORIZED;
import static com.ecommerce.userservice.model.templateAPI.AuthResponseTemplate.UNPROCESSABLE;

/**
 * Defines the OpenAPI specifications for the User Service endpoints.
 * This interface is used by Swagger/Springdoc to generate API documentation.
 */
@SuppressWarnings("ALL")
public interface UserOpenApi {

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200")
    })
    UserDto signUp(UserDto userDto);

    @Operation(summary = "Create token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "422", content = @Content(examples = @ExampleObject(value = UNPROCESSABLE)))
    })
    LoginResponse login(LoginRequest loginDto);

    @Operation(summary = "Test endpoint in order to validate access by right role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(examples = @ExampleObject(value = UNAUTHORIZED))),
            @ApiResponse(responseCode = "403", content = @Content(examples = @ExampleObject(value = UNAUTHORIZED)))
    })
    ResponseEntity<?> data();
}
