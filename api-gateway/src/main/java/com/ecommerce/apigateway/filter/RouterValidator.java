package com.ecommerce.apigateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    public static final List<String> openApiEndpoints = List.of("/sign-up", "/sign-in");

    // Add OpenAPI UI and related paths
    public static final List<String> openApiUIEndpoints = List.of("/swagger-ui.html", "/v3/api-docs");

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints.stream().noneMatch(uri -> request.getURI().getPath().contains(uri))
                    && openApiUIEndpoints.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));

}
