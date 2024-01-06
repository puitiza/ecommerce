package com.ecommerce.userservice.service;

import com.ecommerce.userservice.model.request.LoginRequest;
import com.ecommerce.userservice.model.request.UserDto;
import com.ecommerce.userservice.model.response.LoginResponse;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Optional;

public interface UserService {

    UserDto signUp(UserDto userDto);

    LoginResponse login(LoginRequest loginRequest);

    Optional<UserRepresentation> findUserRepresentationByUsername(String username);
}
