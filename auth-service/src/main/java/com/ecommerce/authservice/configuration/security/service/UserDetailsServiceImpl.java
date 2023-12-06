package com.ecommerce.authservice.configuration.security.service;

import com.ecommerce.authservice.component.exception.errors.UserExceptionErrors;
import com.ecommerce.authservice.component.exception.handler.ExistingElementFoundException;
import com.ecommerce.authservice.model.entity.ERole;
import com.ecommerce.authservice.model.entity.RefreshTokenEntity;
import com.ecommerce.authservice.model.entity.UserEntity;
import com.ecommerce.authservice.model.request.SignUpRequest;
import com.ecommerce.authservice.model.response.JwtResponse;
import com.ecommerce.authservice.model.response.MessageResponse;
import com.ecommerce.authservice.repository.UserRepository;
import com.ecommerce.authservice.service.RoleService;
import com.ecommerce.authservice.util.message_source.MessageSourceHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static com.ecommerce.authservice.component.exception.errors.UserExceptionErrors.EMAIL_FOUND;
import static com.ecommerce.authservice.component.exception.errors.UserExceptionErrors.USERNAME_FOUND;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleService roleService;

    private final PasswordEncoder encoder;
    private final MessageSourceHandler messageSource;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        return UserDetailsImpl.build(user);
    }

    public JwtResponse createToken(UserDetailsImpl userDetails, String jwt, RefreshTokenEntity refreshToken) {
        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtResponse(userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles, jwt, refreshToken.getToken());
    }

    public Optional<UserEntity> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public UserEntity addUser(SignUpRequest request) {

        UserEntity user = new UserEntity(request.getUsername(), request.getEmail(), encoder.encode(request.getPassword()));

        var roleEnum = ERole.valueOf(request.getRole());
        var roleEntity = roleService.getRole(roleEnum).orElse(null);
        var setRole = new HashSet<>(Collections.singletonList(roleEntity));
        user.setRoles(setRole);

        return userRepository.save(user);
    }

    public MessageResponse register(SignUpRequest signUpRequest) {
        var username = userRepository.existsByUsername(signUpRequest.getUsername());
        var email = userRepository.existsByEmail(signUpRequest.getEmail());
        if (Boolean.TRUE.equals(username)) throwException(USERNAME_FOUND);
        if (Boolean.TRUE.equals(email)) throwException(EMAIL_FOUND);

        addUser(signUpRequest);
        return new MessageResponse("User registered successfully!");
    }

    protected void throwException(UserExceptionErrors errors) {
        throw new ExistingElementFoundException(
                messageSource.getLocalMessage(errors.getKey()),
                messageSource.getLocalMessage(errors.getCode()));
    }
}
