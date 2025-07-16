# Spring Cloud Config Server

The Spring Cloud Config Server acts as the central hub for managing application configurations across microservices. It supports storing, versioning, and dynamically delivering configuration settings.

## Key Features
- **Centralized Configuration**: Manage all configurations in one place.
- **Version Control**: Track changes using Git or other backends.
- **Environment-Specific Configurations**: Support for `dev`, `staging`, and `prod` profiles.
- **Multiple Sources**: Integrates with Git, Vault, or custom providers.

## Accessing Configurations
- **REST API**: Use endpoints like `/application/{applicationName}/{profile}` or `/application/{applicationName}/{label}`.
- **Environment Variables**: Automatically injects configuration values into applications.

### Examples
1. Retrieve default configuration for `application`:
   ```bash
   curl --location 'http://localhost:8885/application/default'
   ```
2. Retrieve `dev` configuration for `product-service`:
   ```bash
   curl --location 'http://localhost:8885/product-service/dev'
   ```

## Production Considerations
- **Security**: Secure access with OAuth2 or Keycloak. Use Azure Key Vault for sensitive data (see [docs/production-setup.md](../config/docs/production-setup.md)).
- **Kubernetes**: Deploy with Kubernetes manifests and secrets management (see example in [docs/production-setup.md](../config/docs/production-setup.md)).

## Multi-Module Integration
Configuration for shared DTOs and utilities in the `common` module is managed here. See [docs/multi-module.md](../config/docs/multi-module.md) for details.

## Resources
- [Spring Cloud Config Documentation](https://cloud.spring.io/spring-cloud-config/)
- [Docker Health Checks for Spring Boot](https://medium.com/@aleksanderkolata/docker-spring-boot-and-containers-startup-order-39230e5352a4)