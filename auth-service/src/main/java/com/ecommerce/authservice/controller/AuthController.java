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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public record AuthController(UserDetailsServiceImpl userDetailsService, RefreshTokenService refreshTokenService,
                             AuthenticationManager authenticationManager, JwtUtils jwtUtils) {

    @PostMapping("/sign-in")
    public JwtResponse authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        var refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        log.info("login user: {}", loginRequest.getUsername());
        return userDetailsService.createToken(userDetails, jwt, refreshToken);
    }
    @PostMapping("/sign-up")
    public MessageResponse registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("register user: {}", signUpRequest.getUsername());
        return userDetailsService.register(signUpRequest);
    }

    @PostMapping("/refresh-token")
    public TokenRefreshResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token updated ");
        return refreshTokenService.refreshToken(request, jwtUtils);
    }
}
