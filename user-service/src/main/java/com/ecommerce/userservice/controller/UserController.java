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
@RequestMapping(path = "/user")
public record UserController(UserService userService) implements UserOpenApi {
    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto signUp(@RequestBody UserDto userDto) {
        log.info("REGISTER USER: {}", userDto.getUsername());
        return userService.signUp(userDto);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@Valid @RequestBody LoginRequest loginDto) {
        log.info("LOGIN USER: {}", loginDto.getUsername());
        return userService.login(loginDto);
    }

    @GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> data() {
        log.info("GETTING DATA ENDPOINT TEST");
        return ResponseEntity.ok("Hello world!");
    }
}
