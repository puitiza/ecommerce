package com.ecommerce.userservice.configuration.keycloak;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakInitializerConfigProperties {

    private String authServerUrl;
    private String realm;
    private String resource;
    private String clientKeyPassword;
    private Credentials credentials = new Credentials();

    protected static String masterRealm = "master";
    protected static String clientId = "admin-cli";
    protected static String username = "admin";
    protected static String password = "admin";

    @Getter
    @Setter
    public static class Credentials {
        private String provider;
    }

}
