# Shared Library

A reusable library for common utilities used by microservices in the e-commerce platform, including exception handling
and OpenAPI configuration.

## Overview

The `shared-library` module provides standardized components for:

- **Exception Handling**: Centralized error handling with consistent error responses.
- **OpenAPI Configuration**: Reusable OpenAPI setup for REST API documentation.
- **Localization**: Error message localization via `messages.properties`.

## Structure

- `com.ecommerce.shared.domain`: Core abstractions and models (e.g., `ExceptionError`, `ErrorResponse`).
- `com.ecommerce.shared.application`: Application logic (e.g., `ErrorResponseBuilder`).
- `com.ecommerce.shared.infrastructure`: Technology-specific configurations (e.g., `SharedLibraryConfig`,
  `OpenApiConfigBase`).
- `com.ecommerce.shared.interfaces`: API annotations and templates (e.g., `ApiValidationErrors`, `ResponseApiTemplate`).
- `resources`: Configuration files (e.g., `messages.properties`, OpenAPI response examples).

## Usage

### Dependencies

Add the library to your microservice's `build.gradle`:

```groovy
implementation project(':shared-library')
```