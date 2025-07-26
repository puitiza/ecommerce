# Payment Service

The Payment Service handles payment processing for orders in the e-commerce platform, integrating with external payment
gateways (e.g., Stripe) and communicating with the `order-service` via Kafka events. It uses PostgreSQL for persistent
storage and Keycloak for authentication.

## Key Features

- **Payment Processing**: Processes payments for orders, integrating with external gateways.
- **Authentication & Authorization**: Secured with Keycloak OAuth2 and JWT, requiring authentication for all endpoints
  except public Swagger routes.
- **Event-Driven Integration**: Consumes order events from Kafka to trigger payment processing.
- **Database**: Uses PostgreSQL for storing payment data, with automatic JPA configuration by Spring Boot.
- **Monitoring**: Supports distributed tracing with Zipkin and metrics with Prometheus.

## Configuration

The Payment Service retrieves its configuration from the Spring Cloud Config Server and uses Spring Boot's
autoconfiguration for JPA and OAuth2.

### Security Configuration

The service is secured with Keycloak OAuth2 and JWT. Spring Boot automatically configures a `JwtDecoder` using the
`issuer-uri` to validate tokens. Public endpoints (e.g., Swagger) are accessible without authentication

### Dependencies

- `spring-boot-starter-web`: For REST APIs.
- `spring-boot-starter-data-jpa`: Auto-configures PostgreSQL connectivity.
- `spring-boot-starter-security`: Enables OAuth2 resource server.
- `spring-boot-starter-oauth2-resource-server`: Configures JWT validation.
- `spring-kafka`: For consuming order events from Kafka.
- `spring-cloud-starter-config`: Connects to Config Server.
- `spring-cloud-starter-netflix-eureka-client`: Registers with Eureka.

## Integration with Other Services

- **Config Server**: Retrieves configuration (e.g., database credentials, Keycloak URL) from `http://localhost:8885`.
- **Eureka Server**: Registers as `PAYMENT-SERVICE` for service discovery.
- **API Gateway**: Routes requests from `/payments/**` to this service.
- **Keycloak**: Validates JWT tokens for authenticated requests.
- **Kafka**: Consumes order events (e.g., `ORDER_CREATED`) to initiate payment processing.
- **PostgreSQL**: Stores payment data with automatic schema updates (`ddl-auto: update`).

## Local Setup

1. **Start Dependencies**:
   Ensure Config Server, Eureka Server, Keycloak, PostgreSQL, and Kafka are running via Docker Compose:
   ```bash
   docker-compose up --build -d
   ```

2. **Run the Service**:
   Build and start the Payment Service:
   ```bash
   ./gradlew :payment-service:bootRun
   ```

3. **Access Endpoints**:
    - Swagger UI: `http://localhost:8090/payments/swagger-ui.html` (via API Gateway).
    - Example endpoint: `POST /payments` (requires JWT token).

## Production Considerations

- **HTTPS**: Configure Keycloak to require HTTPS and set `sslRequired` to `external` or `all`.
- **Secrets**: Use Azure Key Vault for sensitive data like database passwords and Keycloak client secrets.
- **Monitoring**: Replace Zipkin with Azure Application Insights for tracing.
- **Database**: Use a managed PostgreSQL instance (e.g., Azure Database for PostgreSQL).
- **Payment Gateway**: Ensure secure integration with production-ready gateways like Stripe.

## Troubleshooting

- **JWT Validation Errors**: Verify `keycloak.realm.url` in the Config Server and ensure Keycloak is running at
  `http://localhost:9090`.
- **Database Connection Issues**: Check PostgreSQL container logs (`docker-compose logs postgres`) and credentials in
  `application.yml`.
- **Kafka Errors**: Ensure Kafka is running (`docker-compose logs kafka`) and topics are created (use AKHQ at
  `http://localhost:8081`).

## Resources

- [Spring Boot OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Kafka](https://docs.spring.io/spring-kafka/reference/html/)
- [Stripe API](https://stripe.com/docs/api)

