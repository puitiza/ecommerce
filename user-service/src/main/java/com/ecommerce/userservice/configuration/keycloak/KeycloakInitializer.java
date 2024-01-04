package com.ecommerce.userservice.configuration.keycloak;

import com.ecommerce.userservice.model.request.UserDto;
import com.ecommerce.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakInitializer implements InitializingBean {

    private final ObjectMapper mapper;

    private final UserService userService;

    private static final String INIT_KEYCLOAK_USERS_PATH = "initializer/init-keycloak-users.json";

    @Override
    public void afterPropertiesSet() {
        initKeycloakUsers();
    }

    private void initKeycloakUsers() {
        List<UserDto> users;
        try {
            Resource resource = new ClassPathResource(INIT_KEYCLOAK_USERS_PATH);
            users = mapper.readValue(
                    resource.getFile(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, UserDto.class));
        } catch (IOException e) {
            String errorMessage = String.format("Failed to read keycloak users : %s", e.getMessage());
            log.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }

        users.forEach(this::initKeycloakUser);
    }

    private void initKeycloakUser(UserDto user) {
        log.info("user detected from json: {}", user.toString());

        var userRepresentation = userService.findUserRepresentationByUsername(user.getUsername())
                .orElse(null);
        if (userRepresentation == null) {
            userService.signUp(user);
            log.info("user registered: {}", user.getUsername());
        } else {
            log.info("user already registered: {}", userRepresentation.getUsername());
        }
    }
}
