package com.ecommerce.shared.infrastructure.configuration;

import com.ecommerce.shared.infrastructure.openapi.ServiceConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class OpenApiConfigBase {

    private static final String SECURITY_SCHEME_NAME = "security_auth";

    @Value("${keycloak.realm.url:}")
    private String keycloakRealmUrl;

    private final ServiceConfig serviceConfig;

    protected OpenApiConfigBase(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    @Bean
    public OpenAPI openApi() {
        OpenAPI openAPI = new OpenAPI()
                .info(getInfo())
                .servers(getServers());

        if (serviceConfig.securityEnabled()) {
            openAPI.components(new Components()
                            .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                    new SecurityScheme()
                                            .name(SECURITY_SCHEME_NAME)
                                            .type(SecurityScheme.Type.OAUTH2)
                                            .flows(new OAuthFlows()
                                                    .clientCredentials(new OAuthFlow()
                                                            .tokenUrl(keycloakRealmUrl + "/protocol/openid-connect/token")
                                                            .scopes(new Scopes().addString("openid", "openid scope"))
                                                    )
                                            )
                            ))
                    .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
        }

        return openAPI;
    }

    private Info getInfo() {
        return new Info()
                .title(serviceConfig.title())
                .description(serviceConfig.description())
                .version(serviceConfig.version());
    }

    private List<Server> getServers() {
        return serviceConfig.servers().stream()
                .map(ServiceConfig.ServerConfig::toServer)
                .collect(Collectors.toList());
    }

}