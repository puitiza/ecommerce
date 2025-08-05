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

/**
 * Component that initializes default Keycloak users upon application startup.
 * It reads user data from a JSON file and registers users if they do not already exist in Keycloak.
 */
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

    /**
     * Reads user data from the `init-keycloak-users.json` file and
     * attempts to register each user in Keycloak if they don't already exist.
     *
     * @throws RuntimeException If the JSON file cannot be read or parsed.
     */
    private void initKeycloakUsers() {
        List<UserDto> users;
        try {
            Resource resource = new ClassPathResource(INIT_KEYCLOAK_USERS_PATH);
            users = mapper.readValue(
                    resource.getInputStream(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, UserDto.class));
        } catch (IOException e) {
            String errorMessage = String.format("Failed to read keycloak users : %s", e.getMessage());
            log.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }

        users.forEach(this::initKeycloakUser);
    }

    /**
     * Checks if a user already exists in Keycloak by username. If not, registers the user.
     *
     * @param user The UserDto representing the user to initialize.
     */
    private void initKeycloakUser(UserDto user) {
        log.info("Processing user from initializer JSON: {}", user.getUsername());

        var userRepresentation = userService.findUserRepresentationByUsername(user.getUsername())
                .orElse(null);
        if (userRepresentation == null) {
            userService.signUp(user);
            log.info("Successfully registered new user: {}", user.getUsername());
        } else {
            log.info("User already registered in Keycloak, skipping: {}", userRepresentation.getUsername());
        }
    }
}
