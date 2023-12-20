package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.model.request.LoginRequest;
import com.ecommerce.authservice.model.request.SignUpRequest;
import com.ecommerce.authservice.model.request.TokenRefreshRequest;
import com.ecommerce.authservice.model.response.JwtResponse;
import com.ecommerce.authservice.model.response.MessageResponse;
import com.ecommerce.authservice.model.response.TokenRefreshResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import static com.ecommerce.authservice.model.templateAPI.AuthResponseTemplate.NOT_FOUND;

@SuppressWarnings("ALL")
public interface AuthOpenApi {

    @Operation(summary = "Authentication of the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(examples = @ExampleObject(value = NOT_FOUND)))
    })
    JwtResponse authenticateUser(LoginRequest loginRequest);

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(examples = @ExampleObject(value = NOT_FOUND)))
    })
    MessageResponse registerUser(SignUpRequest signUpRequest);

    @Operation(summary = "Create a new refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201")
    })
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);


}
