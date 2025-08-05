# Product Service

The Product Service is a core business microservice responsible for managing product information and inventory within
the e-commerce platform. It provides functionalities for creating, reading, updating, and deleting products.

## Key Features

- **Product Management (CRUD)**: Full support for managing product details (name, description, price, etc.).
- **Inventory Management**: Tracks product stock levels.
- **RESTFUL API**: Exposes a comprehensive REST API for product-related operations.
- **Security**: Secured with JWT authentication via Keycloak.

## Technologies

- **Spring Boot 3.0**: Framework for building robust microservices.
- **MySQL**: Relational database for persistent storage of product data.
- **Spring Data JPA / Hibernate**: For data persistence and ORM.
- **Spring Security**: For authentication and authorization using OAuth2 Resource Server.
- **Spring Cloud Config Client**: To fetch centralized configurations.
- **Eureka Client**: For service registration and discovery.
- **Springdoc OpenAPI**: For API documentation and Swagger UI.

## Configuration

The Product Service consumes its configuration from the [Spring Cloud Config Server](../config-server/README.md).

### Application Name and Profile

```yaml
spring:
  application:
    name: product-service # Unique name for Eureka registration
  profiles:
    active: dev # Active profile (e.g., dev, prod)
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8885} # Connects to Config Server
````

### Database Connection (Managed by Config Server)

The database configuration is managed via the [Config Server](https://www.google.com/search?q=config-server/README.md)
to centralize sensitive credentials.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/product_db?useSSL=false&allowPublicKeyRetrieval=true
    username: ${PRODUCT_DB_USERNAME:user} # Injected via environment variable or Vault
    password: ${PRODUCT_DB_PASSWORD:test} # Injected via environment variable or Vault
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update # Configured based on environment (e.g., 'none' for production)
```

**Note:** Database credentials should be provided via environment variables or a secure secrets management system like
HashiCorp Vault in production environments.

### Security (OAuth2 Resource Server)

The Product Service acts as an OAuth2 Resource Server, validating JWTs issued by [Keycloak](https://www.keycloak.org/)
for secure access to its endpoints.

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${keycloak.realm.url}/protocol/openid-connect/certs # JWK Set URI from Keycloak realm
```

### Swagger/OpenAPI Documentation

API documentation is generated using Springdoc OpenAPI and exposed via Swagger UI.

```yaml
springdoc:
  base-path: /products # Base path for API endpoints and Swagger UI
  api-docs:
    enabled: true
    path: ${springdoc.base-path}/v3/api-docs
  swagger-ui:
    enabled: true
    path: ${springdoc.base-path}/swagger-ui.html

permit-urls:
  swagger:
    - ${springdoc.base-path}/v3/api-docs/**
    - ${springdoc.base-path}/swagger-ui/**
    - ${springdoc.base-path}/swagger-ui.html
    - /favicon.ico
```

### Dependencies

- `spring-boot-starter-web`: For REST APIs.
- `spring-boot-starter-data-jpa`: Auto-configures MySQL connectivity.
- `spring-boot-starter-security`: Enables OAuth2 resource server.
- `spring-boot-starter-oauth2-resource-server`: Configures JWT validation.
- `spring-cloud-starter-config`: Connects to Config Server.
- `spring-cloud-starter-netflix-eureka-client`: Registers with Eureka.

## API Endpoints

All API endpoints are prefixed with `/products`. You can explore the available endpoints via the Swagger UI:
`http://localhost:8090/products/swagger-ui.html` (via API Gateway).

## Local Setup

To run the Product Service locally:

1. Ensure [Config Server](https://www.google.com/search?q=config-server/README.md)
   and [Eureka Server](https://www.google.com/search?q=service-registry/README.md) are running.
2. Start the MySQL container (if not using `docker-compose up`).
3. Navigate to the `product-service` directory.
4. Run the application: `./gradlew bootRun` or use your IDE.
5. Alternatively, use `docker-compose up -d product-service` from the root directory to start it as part of the overall
   microservices stack.

## Testing

- **Unit Tests**: Implemented using JUnit 5 and Mockito for isolated component testing.
- **Integration Tests**: Utilizes Testcontainers for database integration testing.
- Run tests with `./gradlew test`.

## Production Considerations

- **HTTPS**: Configure Keycloak to require HTTPS and set `sslRequired` to `external` or `all`.
- **Secrets**: Use Azure Key Vault for sensitive data like database passwords and Keycloak client secrets.
- **Monitoring**: Replace Zipkin with Azure Application Insights for tracing.
- **Database**: Use a managed MySQL instance (e.g., Azure Database for MySQL).

## Multi-Module Integration

The Product Service integrates with the `share-library` module for shared DTOs, exceptions, and utility classes,
ensuring consistency across the microservices' ecosystem.

## Resources

- [Spring Cloud Config Documentation](https://cloud.spring.io/spring-cloud-config/)
- [Spring Boot Data JPA Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html)
- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)