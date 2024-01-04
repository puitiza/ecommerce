package com.ecommerce.userservice.configuration.keycloak;

import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final  KeycloakInitializerConfigProperties configProperties;

    @Bean
    protected Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(configProperties.getAuthServerUrl())
                .grantType(OAuth2Constants.PASSWORD)
                .realm(KeycloakInitializerConfigProperties.masterRealm)
                .clientId(KeycloakInitializerConfigProperties.clientId)
                .username(KeycloakInitializerConfigProperties.username)
                .password(KeycloakInitializerConfigProperties.password)
                .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build()).build();
    }
}
