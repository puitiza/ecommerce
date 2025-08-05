package com.ecommerce.userservice.model.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String authServerUrl;     // Keycloak authentication server URL.
    private String realm;             // Keycloak realm name for this application.
    private String resource;          // Keycloak client ID for this application.
    private String clientKeyPassword; // Client secret for confidential clients.
    private Credentials credentials = new Credentials(); // Nested class for credentials.

    // Constants for Keycloak master realm and admin client used for Keycloak Admin Client setup.
    public static String masterRealm = "master";
    public static String clientId = "admin-cli";
    public static String username = "admin";
    public static String password = "admin";

    @Getter
    @Setter
    public static class Credentials {
        private String provider; // Credential provider type (e.g., "secret").
    }

}
