# Product Service

The Product Service manages the product catalog and inventory for the e-commerce platform, providing CRUD operations for
products. It integrates with Keycloak for authentication, MySQL for persistent storage, and the Spring Cloud Config
Server for centralized configuration.

## Key Features

- **Product Management**: Create, read, update, and delete products and manage inventory.
- **Authentication & Authorization**: Secured with Keycloak OAuth2 and JWT, with role-based access (`ADMIN` for
  create/update/delete, `USER` for browsing).
- **Database**: Uses MySQL for storing product data, with automatic JPA configuration by Spring Boot.
- **Monitoring**: Supports distributed tracing with Zipkin and metrics with Prometheus.

## Configuration

The Product Service retrieves its configuration from the Spring Cloud Config Server and uses Spring Boot's
autoconfiguration for JPA and OAuth2.

### application.yml

```yaml
spring:
  application:
    name: product-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8885}
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${keycloak.realm.url}
  datasource:
    url: ${db.product-service.url:jdbc:mysql://localhost:3306/product_db?useSSL=false&allowPublicKeyRetrieval=true}
    username: ${db.product-service.username:user}
    password: ${db.product-service.password}
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update

server:
  port: 8080

springdoc:
  base-path: /products
  api-docs:
    enabled: true
    path: ${springdoc.base-path}/v3/api-docs
  swagger-ui:
    enabled: true
    path: ${springdoc.base-path}/swagger-ui.html

permit-urls:
  swagger: >
    /products/v3/api-docs/**,
    /products/swagger-ui/**,
    /products/swagger-ui.html,
    /favicon.ico
```

### Security Configuration

The service is secured with Keycloak OAuth2 and JWT. Spring Boot automatically configures a `JwtDecoder` using the
`issuer-uri` to validate tokens. Public endpoints (e.g., Swagger) are accessible without authentication, while
`/admin/**` and `/user/**` require specific roles.

### Dependencies

- `spring-boot-starter-web`: For REST APIs.
- `spring-boot-starter-data-jpa`: Auto-configures MySQL connectivity.
- `spring-boot-starter-security`: Enables OAuth2 resource server.
- `spring-boot-starter-oauth2-resource-server`: Configures JWT validation.
- `spring-cloud-starter-config`: Connects to Config Server.
- `spring-cloud-starter-netflix-eureka-client`: Registers with Eureka.

## Local Setup

1. **Start Dependencies**:
   Ensure Config Server, Eureka Server, Keycloak, and MySQL are running via Docker Compose:
   ```bash
   docker-compose up --build -d
   ```

2. **Run the Service**:
   Build and start the Product Service:
   ```bash
   ./gradlew :product-service:bootRun
   ```

3. **Access Endpoints**:
    - Swagger UI: `http://localhost:8090/products/swagger-ui.html` (via API Gateway).
    - Example endpoints:
        - `GET /products` (public, no authentication).
        - `POST /admin/products` (requires `ADMIN` role).

## Production Considerations

- **HTTPS**: Configure Keycloak to require HTTPS and set `sslRequired` to `external` or `all`.
- **Secrets**: Use Azure Key Vault for sensitive data like database passwords and Keycloak client secrets.
- **Monitoring**: Replace Zipkin with Azure Application Insights for tracing.
- **Database**: Use a managed MySQL instance (e.g., Azure Database for MySQL).

## Troubleshooting

- **JWT Validation Errors**: Verify `keycloak.realm.url` in the Config Server and ensure Keycloak is running at
  `http://localhost:9090`.
- **Database Connection Issues**: Check MySQL container logs (`docker-compose logs mysql`) and credentials in
  `application.yml`.
- **Role Mapping Issues**: Ensure `ADMIN` and `USER` roles are configured in Keycloak’s `ecommerce` realm for the
  `product-service` client.

## Resources

- [Spring Boot OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Cloud Config](https://cloud.spring.io/spring-cloud-config/)