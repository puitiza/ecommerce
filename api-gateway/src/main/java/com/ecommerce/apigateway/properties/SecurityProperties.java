package com.ecommerce.apigateway.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        Map<String, List<EndpointRole>> endpointRoles,
        Map<String, List<PermitUrl>> permitUrls
) {
    public record EndpointRole(String path, String method, String role) {
    }

    public record PermitUrl(String path, List<String> methods) {
    }
}
