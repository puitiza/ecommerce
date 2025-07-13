package com.ecommerce.userservice.service;

import com.ecommerce.userservice.configuration.exception.handler.InvalidUserException;
import com.ecommerce.userservice.model.properties.KeycloakProperties;
import com.ecommerce.userservice.model.request.LoginRequest;
import com.ecommerce.userservice.model.request.UserDto;
import com.ecommerce.userservice.model.response.LoginResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service implementation for user management in the e-commerce platform.
 * Handles user registration, login, and retrieval using Keycloak.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String ADMIN_ROLE = "app_admin";
    private static final String USER_ROLE = "app_user";

    private final Keycloak keycloak;
    private final KeycloakProperties configurationProperties;

    /**
     * Registers a new user in Keycloak with the provided details.
     * This involves creating the user, setting their password, and assigning
     * appropriate realm-level roles (e.g., 'app_admin' or 'app_user').
     *
     * <p>Note: Assigning client-level roles would require additional Keycloak Admin Client
     * operations to retrieve the client and its specific roles.</p>
     * <p>
     * <pre>
     *   {@code
     *     // Get client level role (requires view-clients role)
     *     var clientRepresentation = realmResource.clients().findByClientId(clientId).stream().findFirst().orElseThrow();
     *     var userClientRole = realmResource.clients().get(clientRepresentation.getId()).roles().list()
     *             .stream()
     *             .filter(x -> x.getName().equals("user"))
     *             .findFirst().orElseT
     *     // Assign client level role to user
     *     userResource.roles().clientLevel(clientRepresentation.getId()).add(Collections.singletonList(userClientRole));}
     * </pre>
     *
     * @param userDto The {@link UserDto} containing user registration information.
     * @return The {@link UserDto} object as a confirmation.
     * @throws InvalidUserException if user creation fails (e.g., user already exists, invalid data).
     */
    @Override
    @CircuitBreaker(name = "keycloak", fallbackMethod = "signUpFallback")
    public UserDto signUp(UserDto userDto) {
        log.info("Attempting to register user: {}", userDto.getUsername());

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userDto.getUsername());
        user.setFirstName(userDto.getFirstname());
        user.setLastName(userDto.getLastname());
        user.setEmail(userDto.getEmail());

        RealmResource realmResource = keycloak.realm(configurationProperties.getRealm()); // Get realm : ecommerce
        UsersResource usersResource = realmResource.users();

        Response response = usersResource.create(user);

        if (response.getStatus() == 201) {

            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("Successfully created user with ID: {}", userId);

            // Create password credential for the new user
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(userDto.getPassword());

            UserResource userResource = usersResource.get(userId);

            // Set password credential
            userResource.resetPassword(passwordCred);

            if (userDto.isAdmin())
                assignRol(realmResource, userResource, ADMIN_ROLE);
            else
                assignRol(realmResource, userResource, USER_ROLE);
        } else {
            // Log full response for debugging non-201 status codes
            log.error("Failed to create user in Keycloak. Status: {}, Response: {}", response.getStatus(), response.readEntity(String.class));
            throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatus());
        }
        return userDto;
    }

    /**
     * Fallback method for sign-up failures.
     */
    public UserDto signUpFallback(UserDto userDto, Throwable t) {
        log.error("Sign-up fallback for user {}: {}", userDto.getUsername(), t.getMessage());
        throw new InvalidUserException("Service unavailable, please try again later");
    }

    /**
     * Assigns a specified realm role to a user in Keycloak.
     *
     * @param realmResource The RealmResource instance for the target realm.
     * @param userResource  The UserResource instance for the user to whom the role will be assigned.
     * @param role          The name of the realm role to assign (e.g., "app_admin", "app_user").
     * @throws java.util.NoSuchElementException If the specified role is not found in the realm.
     *                                          Consider a custom exception like `RoleNotFoundException`.
     */
    private void assignRol(RealmResource realmResource, UserResource userResource, String role) {
        var userRealmRole = realmResource.roles().list()
                .stream()
                .filter(x -> x.getName().equals(role)) // Simplified filter
                .findFirst()
                .orElseThrow(() -> new InvalidUserException("Role " + role + " not found in Keycloak realm"));
        userResource.roles().realmLevel().add(Collections.singletonList(userRealmRole));
        log.info("Assigned realm role '{}' to user.", userRealmRole.getName());
    }

    /**
     * Authenticates a user with Keycloak using their username and password and obtains access tokens.
     *
     * @param loginRequest The login request containing username and password.
     * @return A LoginResponse object containing access token, refresh token, and other details.
     * @throws InvalidUserException If authentication fails due to invalid credentials.
     * @throws RuntimeException     For other unexpected errors during the authentication process.
     */
    @Override
    @CircuitBreaker(name = "keycloak", fallbackMethod = "loginFallback")
    public LoginResponse login(LoginRequest loginRequest) {
        Configuration configuration = getConfiguration();
        AuthzClient authzClient = AuthzClient.create(configuration);
        AccessTokenResponse response;
        try {
            response = authzClient.obtainAccessToken(loginRequest.getUsername(), loginRequest.getPassword());
            return new LoginResponse(
                    response.getToken(),
                    response.getRefreshToken(),
                    response.getScope(),
                    response.getExpiresIn(),
                    response.getError()
            );
        } catch (Exception exception) {
            log.error("Authentication failed for user '{}'. Error: {}", loginRequest.getUsername(), exception.getMessage(), exception); // Log stack trace
            throw new InvalidUserException("Invalid user credentials provided.");
        }
    }

    /**
     * Fallback method for login failures.
     */
    public LoginResponse loginFallback(LoginRequest loginRequest, Throwable t) {
        log.error("Login fallback for user {}: {}", loginRequest.getUsername(), t.getMessage());
        throw new InvalidUserException("Service unavailable, please try again later");
    }

    /**
     * Constructs the Keycloak authorization client configuration.
     * This configuration is used to obtain access tokens from Keycloak.
     *
     * @return A Keycloak `Configuration` object.
     */
    private Configuration getConfiguration() {
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", configurationProperties.getClientKeyPassword());
        clientCredentials.put("provider", configurationProperties.getCredentials().getProvider());
        clientCredentials.put("grant_type", OAuth2Constants.PASSWORD);

        return new Configuration(configurationProperties.getAuthServerUrl(),
                configurationProperties.getRealm(), configurationProperties.getResource(), clientCredentials, null);
    }

    /**
     * Finds a user representation in Keycloak by their username.
     *
     * @param username The username to search for.
     * @return An Optional containing the UserRepresentation if found, otherwise empty.
     */
    @Override
    public Optional<UserRepresentation> findUserRepresentationByUsername(String username) {
        return keycloak.realm(configurationProperties.getRealm()).users().search(username)
                .stream().findFirst();
    }
}
