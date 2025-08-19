package com.ecommerce.apigateway.configuration.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for the API Gateway.
 * Provides detailed metadata and OAuth2 security configuration for Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures an OpenAPI bean with detailed metadata and OAuth2 security.
     *
     * @return An OpenAPI instance for the API Gateway.
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API Gateway")
                        .description("API Gateway for the E-Commerce platform, routing requests to microservices " +
                                "(Order Service, Product Service, User Service) with OAuth2 security and rate limiting.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("E-Commerce Team")
                                .url("https://ecommerce.example.com")
                                .email("support@ecommerce.example.com"))
                        .termsOfService("https://ecommerce.example.com/terms")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(getServers());
    }

    private List<Server> getServers() {
        return List.of(
                new Server().url("http://localhost:8090").description("API Gateway"),
                new Server().url("http://localhost:3001").description("Order Service Direct"),
                new Server().url("http://localhost:3002").description("Product Service Direct"),
                new Server().url("http://localhost:3003").description("User Service Direct")
        );
    }
}