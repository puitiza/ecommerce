package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.model.request.LoginRequest;
import com.ecommerce.userservice.model.request.SignUpRequest;
import com.ecommerce.userservice.model.response.LoginResponse;
import com.ecommerce.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public record UserController(UserService userService) {
    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public SignUpRequest signUp(@RequestBody SignUpRequest signUpRequest) {
        log.info("REGISTER USER: {}", signUpRequest.getUsername());
        return userService.signUp(signUpRequest);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginDto) {
        log.info("LOGIN USER: {}", loginDto.getUsername());
        return ResponseEntity.ok(userService.login(loginDto));
    }

    @GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> data() {
        log.info("GETTING DATA ENDPOINT TEST");
        return ResponseEntity.ok(Arrays.asList("Hello world!"));
    }
}
