package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.configuration.security.jwt.JwtUtils;
import com.ecommerce.authservice.configuration.security.service.UserDetailsImpl;
import com.ecommerce.authservice.configuration.security.service.UserDetailsServiceImpl;
import com.ecommerce.authservice.model.request.LoginRequest;
import com.ecommerce.authservice.model.request.SignUpRequest;
import com.ecommerce.authservice.model.request.TokenRefreshRequest;
import com.ecommerce.authservice.model.response.JwtResponse;
import com.ecommerce.authservice.model.response.MessageResponse;
import com.ecommerce.authservice.model.response.TokenRefreshResponse;
import com.ecommerce.authservice.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public record AuthController(UserDetailsServiceImpl userDetailsService, RefreshTokenService refreshTokenService,
                             AuthenticationManager authenticationManager, JwtUtils jwtUtils) implements AuthOpenApi {
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/sign-in", produces = MediaType.APPLICATION_JSON_VALUE)
    public JwtResponse authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("login user: {}", loginRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        var refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        return userDetailsService.createToken(userDetails, jwt, refreshToken);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/sign-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public MessageResponse registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("register user: {}", signUpRequest.getUsername());
        return userDetailsService.register(signUpRequest);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/refresh-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public TokenRefreshResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token updated ");
        return refreshTokenService.refreshToken(request, jwtUtils);
    }

}
