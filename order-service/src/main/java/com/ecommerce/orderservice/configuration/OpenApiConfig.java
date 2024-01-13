package com.ecommerce.orderservice.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@OpenAPIDefinition
@Configuration
//@Profile({"local", "dev"})
public class OpenApiConfig {

    @Value("${server.port}")
    private int serverPort;
    @Bean
    public OpenAPI openApi() {
        final String securitySchemeName = "Auth-JWT";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .servers(servers())
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                .info(new Info()
                        .title("ORDER-SERVICE")
                        .description("This is a sample Spring Boot RESTFul service using springdoc-openapi and OpenAPI 3.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Arun")
                                .url("https://asbnotebook.com")
                                .email("asbnotebook@gmail.com"))
                        .termsOfService("TOC")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org"))
                );
    }

    protected List<Server> servers() {
        Server localServer = new Server();
        localServer.setDescription("local");
        localServer.setUrl("http://localhost:" + serverPort);

        Server testServer = new Server();
        testServer.setDescription("deploy-alpha");
        testServer.setUrl("https://deploy-alpha.org");
        return Arrays.asList(localServer, testServer);
    }
}
