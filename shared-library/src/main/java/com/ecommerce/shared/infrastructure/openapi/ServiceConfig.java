package com.ecommerce.shared.infrastructure.openapi;

import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for OpenAPI documentation.
 */
@ConfigurationProperties(prefix = "openapi")
public record ServiceConfig(
        String title,
        String description,
        String version,
        boolean securityEnabled,
        List<ServerConfig> servers
) {
    public record ServerConfig(String description, String url) {
        /**
         * Converts to an OpenAPI Server object.
         *
         * @return A Server instance.
         */
        public Server toServer() {
            return new Server().description(description).url(url);
        }
    }
}
