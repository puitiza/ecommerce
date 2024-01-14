package com.ecommerce.userservice.service;

import com.ecommerce.userservice.configuration.exception.handler.InvalidUserException;
import com.ecommerce.userservice.model.properties.KeycloakProperties;
import com.ecommerce.userservice.model.request.LoginRequest;
import com.ecommerce.userservice.model.request.UserDto;
import com.ecommerce.userservice.model.response.LoginResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final Keycloak keycloak;
    private final KeycloakProperties configurationProperties;

    /**
     * Creates a new user in Keycloak with the provided sign-up information using the Keycloak Admin Client.
     * <p>
     * If you want to assign a client level role, you'll need this code
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
     * @param userDto The sign-up request containing user details.
     * @return The original signUpRequest object for convenience.
     * @throws RuntimeException If there are errors during user creation or role assignment.
     */
    @Override
    public UserDto signUp(UserDto userDto) {
        log.info("SIGNUP... {}", userDto);

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
            log.info("CREATED USER_ID {}", userId);

            // create password credential
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(userDto.getPassword());

            UserResource userResource = usersResource.get(userId);

            // Set password credential
            userResource.resetPassword(passwordCred);

            if (userDto.isAdmin())
                assignRol(realmResource, userResource, "app_admin");
            else
                assignRol(realmResource, userResource, "app_user");
        }
        return userDto;
    }

    private void assignRol(RealmResource realmResource, UserResource userResource, String role) {
        // Get realm role "tester" (requires view-realm role)
        var userRealmRole = realmResource.roles().list()
                .stream().filter(x -> x.getName().contains("app") && x.getName().equals(role))
                .findFirst().orElseThrow();

        // Assign realm role 'app_user' or 'app_admin' to user
        userResource.roles().realmLevel().add(Collections.singletonList(userRealmRole));
        log.info("ASSIGNED REALM_ROLE {}", userRealmRole);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Configuration configuration = getConfiguration();
        AuthzClient authzClient = AuthzClient.create(configuration);
        AccessTokenResponse response;
        try {
            response = authzClient.obtainAccessToken(loginRequest.getUsername(), loginRequest.getPassword());
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAccessToken(response.getToken());
            loginResponse.setRefreshToken(response.getRefreshToken());
            loginResponse.setScope(response.getScope());
            loginResponse.setExpiresIn(response.getExpiresIn());
            loginResponse.setError(response.getError());
            return loginResponse;
        } catch (Exception exception) {
            log.error("ERROR WITH CREDENTIALS {}", exception.getMessage());
            throw new InvalidUserException("Invalid user credentials");
        }
    }

    private Configuration getConfiguration() {
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", configurationProperties.getClientKeyPassword());
        clientCredentials.put("provider", configurationProperties.getCredentials().getProvider());
        clientCredentials.put("grant_type", OAuth2Constants.PASSWORD);

        return new Configuration(configurationProperties.getAuthServerUrl(),
                configurationProperties.getRealm(), configurationProperties.getResource(), clientCredentials, null);
    }

    @Override
    public Optional<UserRepresentation> findUserRepresentationByUsername(String username) {
        return keycloak.realm(configurationProperties.getRealm()).users().search(username)
                .stream().findFirst();
    }
}
