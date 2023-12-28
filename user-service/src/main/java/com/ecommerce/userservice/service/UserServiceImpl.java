package com.ecommerce.userservice.service;

import com.ecommerce.userservice.model.request.LoginRequest;
import com.ecommerce.userservice.model.request.SignUpRequest;
import com.ecommerce.userservice.model.response.LoginResponse;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService{

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl ;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.resource}")
    private String clientId;
    @Value("${keycloak.client-key-password}")
    private String clientSecret;
    @Value("${keycloak.credentials.provider}")
    private String secret;

    /**
     * Creates a new user in Keycloak with the provided sign-up information using the Keycloak Admin Client.
     * <p>
     * If you want to assign client level role, you'll need this code
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
     * @param signUpRequest The sign-up request containing user details.
     * @return The original signUpRequest object for convenience.
     * @throws RuntimeException If there are errors during user creation or role assignment.
     */
    @Override
    public SignUpRequest signUp(SignUpRequest signUpRequest) {
        log.info("SIGNUP... {}", signUpRequest);

        // (Keycloak configuration and user creation)
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .grantType(OAuth2Constants.PASSWORD)
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build()).build();

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(signUpRequest.getUsername());
        user.setFirstName(signUpRequest.getFirstname());
        user.setLastName(signUpRequest.getLastname());
        user.setEmail(signUpRequest.getEmail());

        RealmResource realmResource = keycloak.realm(realm); // Get realm : ecommerce
        UsersResource usersResource = realmResource.users();

        Response response = usersResource.create(user);

        if (response.getStatus() == 201) {

            String userId = CreatedResponseUtil.getCreatedId(response);
            log.info("CREATED USER_ID {}", userId);

            // create password credential
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(signUpRequest.getPassword());

            UserResource userResource = usersResource.get(userId);

            // Set password credential
            userResource.resetPassword(passwordCred);

            // Get realm role "tester" (requires view-realm role)
            var userRealmRole = realmResource.roles().list()
                    .stream().filter(x -> x.getName().contains("app") && x.getName().equals("app_user"))
                    .findFirst().orElseThrow();

            // Assign realm role 'app_user' to user
            userResource.roles().realmLevel().add(Collections.singletonList(userRealmRole));
            log.info("ASSIGN REALM_ROLE {}", userRealmRole);

        }
        return signUpRequest;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", clientSecret);
        clientCredentials.put("provider", secret);
        clientCredentials.put("grant_type", OAuth2Constants.PASSWORD);

        Configuration configuration = new Configuration(authServerUrl, realm, clientId, clientCredentials, null);
        AuthzClient authzClient = AuthzClient.create(configuration);

        AccessTokenResponse response = authzClient.obtainAccessToken(loginRequest.getUsername(), loginRequest.getPassword());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(response.getToken());
        loginResponse.setRefreshToken(response.getRefreshToken());
        loginResponse.setScope(response.getScope());
        loginResponse.setExpiresIn(response.getExpiresIn());
        loginResponse.setError(response.getError());
        return loginResponse;
    }
}
