package com.ecommerce.userservice.controller.openApi;

import com.ecommerce.shared.openapi.ResponseApiTemplate;
import com.ecommerce.shared.openapi.responses.ApiErrorPostResponses;
import com.ecommerce.userservice.model.request.LoginRequest;
import com.ecommerce.userservice.model.request.UserDto;
import com.ecommerce.userservice.model.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("unused")
public interface UserOpenApi {

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registered user successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(responseCode = "422", description = "Invalid order data request",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNPROCESSABLE))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.RATE_LIMIT)))
    })
    UserDto signUp(UserDto userDto);

    @Operation(summary = "Create token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login user successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))}),
            @ApiResponse(responseCode = "422", description = "Invalid order data request",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNPROCESSABLE))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.RATE_LIMIT)))
    })
    LoginResponse login(LoginRequest loginDto);

    @ApiErrorPostResponses
    @Operation(summary = "Test endpoint in order to validate access by right role")
    @ApiResponse(responseCode = "200")
    ResponseEntity<?> data();
}
