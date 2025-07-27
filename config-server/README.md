# Spring Cloud Config Server

The Spring Cloud Config Server serves as the central hub for managing application configurations across microservices.
It supports storing, versioning, and dynamically delivering configuration settings, enabling seamless
environment-specific setups.

## Key Features

- **Centralized Configuration**: Manage all configurations in one place.
- **Version Control**: Track changes using Git or local filesystem backends.
- **Environment-Specific Configurations**: Support for profiles like `dev` and `prod`.
- **Multiple Sources**: Integrates with Git, local filesystem, or custom providers.

## Configuration Profiles

- **Native Profile**: Activates the local filesystem backend. Configuration files (e.g., `application.yml`,
  `order-service-dev.yml`) are sourced from paths specified in `spring.cloud.config.server.native.search-locations` (
  e.g., `classpath:config/shared/`, `classpath:config/`). Ideal for local development.
- **Git Profile**: Activates the Git backend. Configuration files are retrieved from a Git repository specified in
  `spring.cloud.config.server.git.uri` (e.g., `https://github.com/puitiza/ecommerce-configurations.git`). Suitable for
  production.

**Note**: Profiles like `dev` and `prod` are used by client microservices to request specific configuration files (e.g.,
`order-service-dev.yml` for `dev`, `order-service-prod.yml` for `prod`) from the config server.

## Accessing Configurations

- **REST API**: Use endpoints like `/{application}/{profile}` to fetch configurations.
- **Environment Variables**: Automatically injects configuration values into applications.

### Examples

1. Retrieve default configuration for `application`:
   ```bash
   curl http://localhost:8885/application/default
   ```
2. Retrieve `dev` configuration for `order-service`:
   ```bash
   curl http://localhost:8885/order-service/dev
   ```

## Setup Recommendations

- **Local Development**:
    - Use the `native` profile with volumes (e.g., `./config/shared`, `./config`) for editing configurations without
      rebuilding.
    - Alternatively, include configuration files in the JAR (`src/main/resources/config/shared`,
      `src/main/resources/config`) and skip volumes.
- **Production**:
    - Use the `git` profile with a remote Git repository (e.g.,
      `https://github.com/puitiza/ecommerce-configurations.git`).
    - No local volumes are needed, as configurations are fetched from the Git repository.

### Example Configuration (`application.yml`)

```yaml
spring:
  application:
    name: config-server
  profiles:
    active: ${SPRING_PROFILE:native}
  cloud:
    config:
      server:
        git: # Get files from Git repository
          uri: ${CONFIG_SERVER_GIT_URI:https://github.com/puitiza/ecommerce-configurations.git}
        native: # Get files from local filesystem
          search-locations: classpath:config/shared/,classpath:config/
server:
  port: 8885
```

Check environment variables of a running service:

```bash
docker exec -it user-service env
```

## Production Considerations

- **Security**: Secure access with OAuth2 or Keycloak. Use Azure Key Vault for sensitive data (
  see [docs/production-setup.md](../config/docs/production-setup.md)).
- **Kubernetes**: Deploy with Kubernetes manifests and secrets management (see example
  in [docs/production-setup.md](../config/docs/production-setup.md)).

## Multi-Module Integration

Configuration for shared DTOs and utilities in the `shared-library` module is managed here.
See [docs/multi-module.md](../config/docs/multi-module.md) for details.

## Resources

- [Spring Cloud Config Documentation](https://cloud.spring.io/spring-cloud-config/)
- [Docker Health Checks for Spring Boot](https://medium.com/@aleksanderkolata/docker-spring-boot-and-containers-startup-order-39230e5352a4)