package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.controller.openApi.UserOpenApi;
import com.ecommerce.userservice.model.request.LoginRequest;
import com.ecommerce.userservice.model.request.UserDto;
import com.ecommerce.userservice.model.response.LoginResponse;
import com.ecommerce.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public record UserController(UserService userService) implements UserOpenApi {
    /**
     * Handles user registration requests.
     *
     * @param userDto The UserDto containing details for the new user.
     * @return The UserDto of the registered user.
     */
    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto signUp(@RequestBody UserDto userDto) {
        log.info("Received signup request for user: {}", userDto.getUsername());
        return userService.signUp(userDto);
    }

    /**
     * Handles user login requests, authenticating with Keycloak and returning access tokens.
     *
     * @param loginDto The LoginRequest containing username and password.
     * @return A LoginResponse with access and refresh tokens.
     */
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@Valid @RequestBody LoginRequest loginDto) {
        log.info("Received login request for user: {}", loginDto.getUsername());
        return userService.login(loginDto);
    }

    /**
     * A test endpoint to validate access based on user roles.
     * Requires authentication and appropriate roles as defined by Spring Security.
     *
     * @return A ResponseEntity with a "Hello world!" message if access is granted.
     */
    @GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> data() {
        log.info("Accessing secured /data endpoint.");
        return ResponseEntity.ok("Hello world!");
    }
}
