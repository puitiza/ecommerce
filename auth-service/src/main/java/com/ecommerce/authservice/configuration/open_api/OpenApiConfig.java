package com.ecommerce.authservice.configuration.open_api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;


@OpenAPIDefinition
@Configuration
public class OpenApiConfig {

    @Value("${server.port}")
    private int serverPort;
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .servers(servers())
                .info(new Info()
                        .title("AUTH-SERVICE")
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
        testServer.setDescription("gateway");
        testServer.setUrl("https://localhost:8090");
        return Arrays.asList(localServer, testServer);
    }
}
