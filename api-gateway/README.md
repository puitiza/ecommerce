# API Gateway
The API Gateway, built with **Spring Cloud Gateway**, handles HTTP traffic between microservices, providing security features like JWT authentication, CORS, rate limiting, and token relay.

## Key Features
- **JSON Web Tokens (JWT)**: Securely transmits information using a signed token (header, payload, signature).
- **CORS**: Enables cross-origin requests from authorized domains.
- **Rate Limiting**: Controls incoming request rates to prevent overload.
- **Token Relay**: Forwards authentication tokens to downstream services.

## Configuration
### CORS
Enables cross-origin requests for authorized domains (e.g., `http://localhost:8090` for Swagger UI).

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:8090"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
```

### Rate Limiting
Uses Redis for rate limiting in local/dev environments. In production, Azure API Gateway can handle rate limiting (see [docs/production-setup.md](../config/docs/production-setup.md)).

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: rate-limited-route
          uri: https://example.org
          filters:
            - name: RequestRateLimiter
              args:
                key-resolver: "#{@hostAddressKeyResolver}"
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                redis-rate-limiter.requestedTokens: 1
```

### Token Relay
Forwards authentication tokens to downstream services.

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - TokenRelay=
```

### OAuth2 Client Configuration
Integrates with Keycloak for authentication.

```groovy
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
```

```yaml
spring:
  security:
    oauth2:
      client:
        provider:
          keycloak:
            issuer-uri: ${KEYCLOAK_SERVER_URL:http://localhost:9090}/realms/ecommerce
            user-name-attribute: preferred_username
        registration:
          ecommerce:
            provider: keycloak
            client-id: api-gateway-client
            client-secret: fB6qaEazuI3oZJYHB1lu91Py6FbKP1m6
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
```

## Production Considerations
- **Azure API Gateway**: Replace Spring Cloud Gateway rate limiting with Azure API Gateway policies.
- **Disable Redis**: Remove Redis dependency in production (see [docs/production-setup.md](../config/docs/production-setup.md)).
- **Tracing**: Use Azure Application Insights instead of Zipkin for production tracing.

## Multi-Module Integration
The API Gateway uses shared DTOs and exceptions from the `common` module for consistent data models and error handling across services. See [docs/multi-module.md](../config/docs/multi-module.md) for details.

## Resources
- [Spring Cloud Gateway Security with JWT](https://medium.com/@rajithgama/spring-cloud-gateway-security-with-jwt-23045ba59b8a)
- [API Gateway in Spring Boot](https://medium.com/@ankithahjpgowda/api-gateway-in-spring-boot-3ea804003021)
- [Central Swagger in Spring Cloud Gateway](https://medium.com/@oguz.topal/central-swagger-in-spring-cloud-gateway-697a1c37b03d)
- [Swagger Integration with Spring Cloud Gateway](https://medium.com/@pubuduc.14/swagger-openapi-specification-3-integration-with-spring-cloud-gateway-part-2-1d670d4ab69a)
- [Spring Cloud Gateway OAuth2 with Keycloak](https://blog.devops.dev/spring-cloud-gateway-oauth2-security-with-keycloak-jwt-tokens-and-securing-it-with-https-ssl-2166d8009531)
- [Rate Limiting with Redis](https://medium.com/@htyesilyurt/implement-rate-limiting-in-spring-cloud-gateway-with-redis-7b71c8dd53a3)
- [Official Token Relay](https://cloud.spring.io/spring-cloud-static/spring-cloud-security/2.1.3.RELEASE/single/spring-cloud-security.html#_token_relay)