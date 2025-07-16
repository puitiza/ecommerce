# Multi-Module Project Setup

The e-commerce project uses a Gradle multi-module structure to share code and configurations across microservices, improving maintainability and consistency.

## Structure
- **Root Project**: Contains the main `build.gradle` and `settings.gradle` for dependency management and module configuration.
- **Common Module**: A shared module (`common`) for reusable code, including:
  - **DTOs**: Data Transfer Objects for consistent data models (e.g., `UserDTO`, `OrderDTO`).
  - **Exceptions**: Custom exceptions for standardized error handling (e.g., `ResourceNotFoundException`).
  - **Utilities**: Shared utilities for logging, validation, or serialization.
- **Service Modules**: Individual modules for each microservice (e.g., `api-gateway`, `user-service`).

## Root `build.gradle` Example
```groovy
subprojects {
    apply plugin: 'java'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation project(':common')
        implementation 'org.springframework.boot:spring-boot-starter'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
    }
}
```

## Common Module
### DTOs
Shared DTOs ensure consistent data exchange. Example:
```java
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    // Getters and setters
}
```

### Exceptions
Custom exceptions for consistent error handling:
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### Utilities
Shared utilities, e.g., for JSON serialization or logging.

## Usage
- Add the `common` module as a dependency in each service's `build.gradle`:
  ```groovy
  dependencies {
      implementation project(':common')
  }
  ```
- Use shared DTOs and exceptions in service implementations.

## Benefits
- **Consistency**: Standardized data models and error handling.
- **Reusability**: Avoid duplicating code across services.
- **Maintainability**: Centralized updates for shared logic.

## Future Enhancements
- Add more shared utilities (e.g., Kafka event serializers).
- Implement centralized validation logic for DTOs.
- Extend the `common` module for cross-cutting concerns like security or monitoring.

## Resources
- [Gradle Multi-Project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [Spring Boot with Gradle](https://spring.io/guides/gs/multi-module/)