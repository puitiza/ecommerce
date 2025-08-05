# API Gateway

The API Gateway, built with **Spring Cloud Gateway**, handles HTTP traffic between microservices, providing security
features like JWT authentication, CORS, rate limiting, and token relay.

## Key Features

- **JSON Web Tokens (JWT)**: Securely transmits information using a signed token (header, payload, signature), used for
  authentication and authorization.
- **CORS (Cross-Origin Resource Sharing)**: Enables cross-origin requests from authorized domains to allow front-end
  applications to interact with the API.
- **Rate Limiting**: Controls incoming request rates to prevent overload and protect backend services.
- **Token Relay**: Forwards authentication tokens received by the Gateway to downstream services, allowing them to use
  the same security context.
- **Dynamic Routing**: Routes incoming requests to the appropriate microservice based on defined predicates.

## Configuration

The API Gateway fetches its core configuration from the [Spring Cloud Config Server](../config-server/README.md).

### CORS

Configures `globalcors` to enable cross-origin requests for authorized domains. This is particularly important for
Swagger UI (e.g., `http://localhost:8090`) and any front-end applications.

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "${CORS_ALLOWED_ORIGINS:http://localhost:8090}"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
````

### Rate Limiting

Utilizes Redis for request rate limiting in local and development environments. In production, it's recommended to
offload this to a cloud-managed API Gateway solution like Azure API Gateway.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: USER-SERVICE
          uri: lb://USER-SERVICE
          predicates:
            - Path=/users/**
          filters:
            - name: RequestRateLimiter
              args:
                key-resolver: "#{@hostAddressKeyResolver}"
                redis-rate-limiter:
                  replenishRate: 1
                  burstCapacity: 2
```

### Token Relay

The `TokenRelay` filter is crucial for propagating the OAuth2 token received by the API Gateway to downstream
microservices, ensuring a consistent security context across the call chain.

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - TokenRelay=
```

### OAuth2 Client Configuration

The API Gateway integrates with [Keycloak](https://www.keycloak.org/) for OAuth2-based authentication and authorization.
It acts as an OAuth2 client, handling the authentication flow and validating JWTs.

```groovy
// build.gradle (dependencies)
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
```

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${keycloak.realm.url} # e.g., http://localhost:9090/realms/ecommerce
      client:
        provider:
          keycloak:
            issuer-uri: ${keycloak.realm.url} # The Keycloak server's issuer URI
            user-name-attribute: preferred_username # Attribute to extract username from JWT
        registration:
          ecommerce: # Registration ID for the API Gateway client
            provider: keycloak
            client-id: api-gateway-client # The client ID registered in Keycloak
            client-secret: ${keycloak.client-secrets.api-gateway-client} # Sensitive secret from Config Server/environment
            authorization-grant-type: authorization_code # Standard OAuth2 flow
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}" # Redirect URI after successful authentication
```

### Routing

The Gateway dynamically routes requests to appropriate microservices registered
with [Eureka Server](https://www.google.com/search?q=service-registry/README.md).

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: PRODUCT-SERVICE # Route ID
          uri: lb://PRODUCT-SERVICE # Load-balanced URI to Eureka-registered service
          predicates:
            - Path=/products/** # Matches requests starting with /products/
        - id: ORDER-SERVICE
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/orders/**
        - id: USER-SERVICE
          uri: lb://USER-SERVICE
          predicates:
            - Path=/users/**
```

## Production Considerations

- **Azure API Gateway**: For production deployments, consider replacing Spring Cloud Gateway's rate limiting and
  potentially other features with Azure API Gateway policies for better scalability and management.
- **Disable Redis**: If Azure API Gateway handles rate limiting, Redis dependency for rate limiting can be removed in
  production environments.
- **Tracing**: Transition from Zipkin to Azure Application Insights for comprehensive distributed tracing in production.
- **HTTPS**: **Crucial for production deployments.** Ensure HTTPS is enforced for all traffic to the API Gateway and
  between the Gateway and downstream services. Configure Keycloak for `sslRequired: external` or `all`.

## Multi-Module Integration

The API Gateway leverages shared DTOs, exceptions, and utility classes from the `common` module to ensure consistent
data models and error handling across all microservices. Refer
to [docs/multi-module.md](https://www.google.com/search?q=../config/docs/multi-module.md) for further details.

## Resources

- [Spring Cloud Gateway Security with JWT](https://medium.com/@rajithgama/spring-cloud-gateway-security-with-jwt-23045ba59b8a)
- [API Gateway in Spring Boot](https://medium.com/@ankithahjpgowda/api-gateway-in-spring-boot-3ea804003021)
- [Central Swagger in Spring Cloud Gateway](https://medium.com/@oguz.topal/central-swagger-in-spring-cloud-gateway-697a1c37b03d)
- [Swagger Integration with Spring Cloud Gateway](https://medium.com/@pubuduc.14/swagger-openapi-specification-3-integration-with-spring-cloud-gateway-part-2-1d670d4ab69a)
- [Spring Cloud Gateway OAuth2 with Keycloak](https://blog.devops.dev/spring-cloud-gateway-oauth2-security-with-keycloak-jwt-tokens-and-securing-it-with-https-ssl-2166d8009531)
- [Rate Limiting with Redis](https://medium.com/@htyesilyurt/implement-rate-limiting-in-spring-cloud-gateway-with-redis-7b71c8dd53a3)
- [Official Token Relay](https://cloud.spring.io/spring-cloud-static/spring-cloud-security/2.1.3.RELEASE/single/spring-cloud-security.html#_token_relay)