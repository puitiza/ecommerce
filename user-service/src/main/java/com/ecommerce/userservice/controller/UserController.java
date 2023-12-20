package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.model.request.LoginRequest;
import com.ecommerce.userservice.model.request.SignUpRequest;
import com.ecommerce.userservice.model.response.LoginResponse;
import com.ecommerce.userservice.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping(path = "/users")
public record UserController(UserService userService) {
    @PostMapping(
            path = "/signup",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public SignUpRequest signUp(@RequestBody SignUpRequest signUpRequest) {
        return userService.signUp(signUpRequest);
    }

    @PostMapping(path = "/login", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginDto) {
        return ResponseEntity.ok(userService.login(loginDto));
    }

    @GetMapping(path = "/data")
    public ResponseEntity<?> data() {
        return ResponseEntity.ok(Arrays.asList("Hello world!"));
    }
}
