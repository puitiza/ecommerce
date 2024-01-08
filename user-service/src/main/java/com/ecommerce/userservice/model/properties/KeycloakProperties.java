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

    private String authServerUrl;
    private String realm;
    private String resource;
    private String clientKeyPassword;
    private Credentials credentials = new Credentials();

    public static String masterRealm = "master";
    public static String clientId = "admin-cli";
    public static String username = "admin";
    public static String password = "admin";

    @Getter
    @Setter
    public static class Credentials {
        private String provider;
    }

}
