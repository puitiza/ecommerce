# Order Service

The Order Service is a core component of the e-commerce platform, responsible for managing order creation, processing,
and state transitions. It integrates with Keycloak for authentication, MySQL for persistent storage, Kafka for
event-driven communication, and the Spring Cloud Config Server for centralized configuration.

## Key Features

- **Order Management**: Create, retrieve, update, and cancel orders with a state machine for lifecycle management (
  see [docs/order-state-machine.md](../config/docs/order-state-machine.md)).
- **Authentication & Authorization**: Secured with Keycloak OAuth2 and JWT, requiring authentication for all endpoints
  except public Swagger routes.
- **Event-Driven Integration**: Publishes order events to Kafka for consumption by `payment-service`,
  `shipment-service`, and `notification-service`.
- **Database**: Uses MySQL for storing order data, with automatic JPA configuration by Spring Boot.
- **Monitoring**: Supports distributed tracing with Zipkin and metrics with Prometheus.

## Configuration

The Order Service retrieves its configuration from the Spring Cloud Config Server and uses Spring Boot's
autoconfiguration for JPA, OAuth2, and Kafka.

### Security Configuration

The service is secured with Keycloak OAuth2 and JWT. Spring Boot automatically configures a `JwtDecoder` using the
`issuer-uri` to validate tokens. Public endpoints (e.g., Swagger) are accessible without authentication.

### Dependencies

- `spring-boot-starter-web`: For REST APIs.
- `spring-boot-starter-data-jpa`: Auto-configures MySQL connectivity.
- `spring-boot-starter-security`: Enables OAuth2 resource server.
- `spring-boot-starter-oauth2-resource-server`: Configures JWT validation.
- `spring-kafka`: For event publishing to Kafka.
- `spring-cloud-starter-config`: Connects to Config Server.
- `spring-cloud-starter-netflix-eureka-client`: Registers with Eureka.

## Integration with Other Services

- **Config Server**: Retrieves configuration (e.g., database credentials, Keycloak URL) from `http://localhost:8885`.
- **Eureka Server**: Registers as `ORDER-SERVICE` for service discovery.
- **API Gateway**: Routes requests from `/orders/**` to this service.
- **Keycloak**: Validates JWT tokens for authenticated requests.
- **Kafka**: Publishes order events (e.g., `ORDER_CREATED`, `ORDER_CANCELLED`) to topics consumed by `payment-service`,
  `shipment-service`, and `notification-service`.
- **MySQL**: Stores order data with automatic schema updates (`ddl-auto: update`).

## Local Setup

1. **Start Dependencies**:
   Ensure Config Server, Eureka Server, Keycloak, MySQL, and Kafka are running via Docker Compose:
   ```bash
   docker-compose up --build -d
   ```

2. **Run the Service**:
   Build and start the Order Service:
   ```bash
   ./gradlew :order-service:bootRun
   ```

3. **Access Endpoints**:
    - Swagger UI: `http://localhost:8090/orders/swagger-ui.html` (via API Gateway).
    - Example endpoint: `GET /orders` (requires JWT token).

## Production Considerations

- **HTTPS**: Configure Keycloak to require HTTPS and set `sslRequired` to `external` or `all`.
- **Secrets**: Use Azure Key Vault for sensitive data like database passwords and Keycloak client secrets.
- **Monitoring**: Replace Zipkin with Azure Application Insights for tracing.
- **Database**: Use a managed MySQL instance (e.g., Azure Database for MySQL).
- **Kafka**: Deploy a managed Kafka cluster (e.g., Confluent Cloud or Azure Event Hubs).

## Troubleshooting

- **JWT Validation Errors**: Verify `keycloak.realm.url` in the Config Server and ensure Keycloak is running at
  `http://localhost:9090`.
- **Database Connection Issues**: Check MySQL container logs (`docker-compose logs mysql`) and credentials in
  `application.yml`.
- **Kafka Errors**: Ensure Kafka is running (`docker-compose logs kafka`) and topics are created (use AKHQ at
  `http://localhost:8081`).

## Resources

- [Spring Boot OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Spring Kafka](https://docs.spring.io/spring-kafka/reference/html/)
- [Spring Cloud Config](https://cloud.spring.io/spring-cloud-config/)
- [Keycloak OAuth2](https://www.keycloak.org/docs/latest/securing_apps/)