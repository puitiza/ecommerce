package com.ecommerce.shared.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

public abstract class OpenApiConfigBase {

    private static final String SECURITY_SCHEME_NAME = "security_auth";

    @Value("${keycloak.realm.url:}")
    private String keycloakRealmUrl;

    private final ServiceConfig serviceConfig;

    protected OpenApiConfigBase(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public OpenAPI openApi() {
        OpenAPI openAPI = new OpenAPI()
                .info(getInfo())
                .servers(getServers());

        if (serviceConfig.securityEnabled()) {
            openAPI.components(getComponents())
                    .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
        }

        return openAPI;
    }

    protected abstract Info getInfo();

    protected abstract List<Server> getServers();

    private Components getComponents() {
        return new Components()
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
                );
    }
}