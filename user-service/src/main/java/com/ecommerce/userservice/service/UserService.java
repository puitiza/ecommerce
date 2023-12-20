package com.ecommerce.userservice.service;

import com.ecommerce.userservice.model.request.LoginRequest;
import com.ecommerce.userservice.model.request.SignUpRequest;
import com.ecommerce.userservice.model.response.LoginResponse;

public interface UserService {

    SignUpRequest signUp(SignUpRequest signUpRequest);

    LoginResponse login(LoginRequest loginRequest);
}
