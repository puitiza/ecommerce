package com.ecommerce.productservice.configuration.openApi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@OpenAPIDefinition
@Configuration
public class OpenApiConfig {

    @Value("${keycloak.realm.url}")
    private String keycloakRealmUrl;

    @Bean
    public OpenAPI openApi() {
        final String securitySchemeName = "security_auth"; // A unique name for this scheme

        return new OpenAPI()
                .info(new Info()
                        .title("Product Service APIs")
                        .description("This lists all the Product Service API Calls. The Calls are OAuth2 secured, "
                                + "so please use your client ID and Secret to test them out.")
                        .version("v1.0"))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.OAUTH2)
                                                .flows(new OAuthFlows()
                                                        .clientCredentials(new OAuthFlow()
                                                                .tokenUrl(keycloakRealmUrl + "/protocol/openid-connect/token")
                                                                .scopes(new Scopes()
                                                                        .addString("openid", "openid scope"))
                                                        )
                                                )
                                )
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .servers(serverList());
    }

    private List<Server> serverList() {
        Server localServer = new Server();
        localServer.setDescription("gateway");
        localServer.setUrl("http://localhost:8090");

        // Add other servers if needed
        Server localServer_2 = new Server();
        localServer_2.setDescription("local");
        localServer_2.setUrl("http://localhost:4005");
        return Arrays.asList(localServer, localServer_2);
    }
}
