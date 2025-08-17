# API Curl Examples for Order Service

This document contains curl commands for Order Service-specific scenarios (e.g., validation, business rules).
Gateway-level errors (401/403/429/503/500) are in [curl-gateway-scenarios.md](curl-gateway-scenarios.md).

## Assumptions

- **Base URL**: The service runs via a gateway at `http://localhost:8090` (update for your environment).
- **Authentication**: Replace `••••••` with a valid JWT token in the `Authorization` header.
- **Content Type**: Requests use `application/json` where applicable.
- **Error Codes**:
    - `ORD-xxx`: Order-specific errors (e.g., `ORD-002` for duplicate products, `ORD-004` for product availability
      issues).
    - `GEN-xxx`: General errors from the order context (e.g., `GEN-001` for validation, `GEN-002` for not found).
- **Tracing**: Add `?trace=true` to the URL for detailed stack traces in development mode (not recommended for
  production).
- **HTTP Statuses**: Most scenarios return 400 (validation errors), 422 (semantic/business rule errors), or 404 (not
  found).

## Order Creation Scenarios

### Scenario 1: Create Order - Product Not Found

**Description**: Attempt to create an order with a non-existent product ID, expecting 404 Not Found.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ••••••' \
--data '{
    "items": [
      { "productId": 2, "quantity": 10 },
      { "productId": 3, "quantity": 5 }
    ],
    "shippingAddress": "123 Main St, Anytown, CA 12345"
}'
```

**Expected Response**:

```json
{
  "status": 404,
  "errorCode": "GEN-002",
  "message": "Product with id 999 not found",
  "timestamp": "2025-08-12T16:17:54.863491508Z"
}
```

**Notes**:

- Tests product lookup failure in the product service.
- Error code `GEN-002` indicates a resource not found, handled by the order service.

### Scenario 2: Create Order - Missing Shipping Address

**Description**: Attempt to create an order without a shipping address, expecting 400 Bad Request for validation error.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders?trace=true' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
--data '{
    "items": [
      { "quantity": 10 },
      { "productId": 3 }
    ]
}'
```

**Expected Response**:

```json
{
  "status": 400,
  "errorCode": "GEN-001",
  "message": "Validation error: Validation failed for multiple fields.",
  "details": "Multiple validation errors occurred. Please check the 'errors' field for details on each validation failure.",
  "timestamp": "2025-08-17T01:27:37.057551096Z",
  "stackTrace": [
    "org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor.resolveArgument(RequestResponseBodyMethodProcessor.java:159)",
    "org.springframework.web.method.support.HandlerMethodArgumentResolverComposite.resolveArgument(HandlerMethodArgumentResolverComposite.java:122)",
    "..."
  ],
  "errors": [
    {
      "field": "items[1].quantity",
      "message": "Quantity is required"
    },
    {
      "field": "items[0].productId",
      "message": "Product ID is required"
    },
    {
      "field": "shippingAddress",
      "message": "Shipping address is required"
    }
  ]
}
```

**Notes**:

- Tests JSON schema validation for the request body.
- Error code `GEN-001` indicates a syntactic validation failure.
- HTTP 400 is appropriate for syntax errors.

### Scenario 3: Create Order - Duplicate Product ID

**Description**: Attempt to create an order with duplicate product IDs, expecting a 422 Unprocessable Entity for a
business rule violation.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ••••••' \
--data '{
    "items": [
      { "productId": 2, "quantity": 10 },
      { "productId": 2, "quantity": 5 }
    ],
    "shippingAddress": "123 Main St, Anytown, CA 12345"
}'
```

**Expected Response**:

```json
{
  "status": 422,
  "errorCode": "ORD-002",
  "message": "Duplicate product found with id: 2 in the order",
  "timestamp": "2025-08-12T16:22:32.731326887Z"
}
```

**Notes**:

- Error code `ORD-002` is specific to the order service for duplicate product validation.
- HTTP 422 is used to indicate a semantic/business rule violation, distinguishing it from syntax errors (400).

## Error Handling Scenarios

### Scenario 4: Get Order by ID - Invalid UUID Format

**Description**: Attempt to retrieve an order with an invalid UUID format, expecting a 400 Bad Request.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders/0' \
--header 'Authorization: Bearer ••••••'
```

**Expected Response**:

```json
{
  "status": 400,
  "errorCode": "GEN-001",
  "message": "Validation error: Invalid value '0' for parameter 'orderId'. Expected type: UUID",
  "details": "Method parameter 'orderId': Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: 0",
  "timestamp": "2025-08-12T16:21:14.608568628Z"
}
```

**Notes**:

- Tests path variable validation for UUID format.
- Error code `GEN-001` indicates a syntactic validation error.
- HTTP 400 is appropriate for syntax-related issues.

### Scenario 5: Search Orders - Missing Required Parameter

**Description**: Attempt to search orders without the required `active` query parameter, expecting a 400 Bad Request.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders/search' \
--header 'Authorization: Bearer ••••••'
```

**Expected Response**:

```json
{
  "status": 400,
  "errorCode": "GEN-001",
  "message": "Validation error: Missing required parameter 'active' of type 'boolean'",
  "details": "Required request parameter 'active' for method parameter type boolean is not present",
  "timestamp": "2025-08-12T16:30:29.725450385Z"
}
```

**Notes**:

- Tests query parameter validation.
- Error code `GEN-001` is used for syntactic validation failures.
- HTTP 400 is appropriate.

### Scenario 6: Search Orders - Invalid Path with Trace (Dev Mode)

**Description**: Attempt to search orders with an invalid path and enable stack trace for debugging, expecting a 400 Bad
Request.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders/search?trace=true' \
--header 'Authorization: Bearer ••••••'
```

**Expected Response**:

```json
{
  "status": 400,
  "errorCode": "GEN-001",
  "message": "Validation error: Invalid value 'search' for parameter 'orderId'. Expected type: UUID",
  "details": "Method parameter 'orderId': Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: search",
  "timestamp": "2025-08-12T16:32:43.658896086Z",
  "stackTrace": [
    "org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver.convertIfNecessary(...)",
    "org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver.resolveArgument(...)",
    "..."
  ]
}
```

**Notes**:

- Tests dev-mode tracing for debugging invalid path parameters.
- Error code `GEN-001` indicates a syntactic validation error.
- Stack trace is included only when `?trace=true` is provided (not for production).

### Scenario 7: Create Order - Product Availability Check Failed

**Description**: Attempt to create an order where the product service fails to verify product availability (e.g., due to
a 4xx or 5xx error from the product service, excluding 503).

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ••••••' \
--data '{
    "items": [
        {"productId": 2, "quantity": 10},
        {"productId": 3, "quantity": 5}
    ],
    "shippingAddress": "123 Main St, Anytown, CA 12345"
}'
```

**Expected Response**:

```json
{
  "status": 400,
  "errorCode": "ORD-004",
  "message": "Could not verify product availability for productId: 2,3",
  "details": "product-service executing POST http://product-service/products/batch",
  "timestamp": "2025-08-16T23:07:14.171330717Z"
}
```

**Notes**:

- Triggered when the product service returns a 4xx or 5xx error (excluding 503) during availability checks.
- Error code `ORD-004` corresponds to `ExceptionError.ORDER_PRODUCT_AVAILABILITY_CHECK_FAILED`.
- Consider using HTTP 422 for semantic errors (e.g., business rule violations) instead of 400 to distinguish from syntax
  errors.

## Other Endpoints

- **GET /orders**: Retrieve paginated orders (add scenarios for pagination parameters).
- **PUT /orders/{id}**: Update an existing order (add scenarios for valid/invalid updates).
- **DELETE /orders/{id}**: Cancel an order (add scenarios for valid/invalid cancellations).

## Notes on Error Codes and Statuses

- **Order-Specific Errors** (`ORD-xxx`):
    - `ORD-002`: Duplicate product IDs in the order (422).
    - `ORD-004`: Product availability check failed (400, consider 422 for semantic errors).
- **General Errors** (`GEN-xxx`):
    - `GEN-001`: Syntactic validation errors (400).
    - `GEN-002`: Resource not found (404).
- **HTTP Status Recommendations**:
    - Use 400 for syntax errors (e.g., invalid JSON, missing required fields).
    - Use 422 for semantic/business rule violations (e.g., duplicate products, product availability issues).
    - Use 404 for resource not found (e.g., product or order not found).
- **Feign Exception Handling**:
    - The `ProductFeignAdapter` maps Feign 503/-1 to `ORD-004` with `SERVICE_UNAVAILABLE` (400, consider 503).
    - Other Feign 4xx/5xx errors map to `ORD-004` with `ORDER_PRODUCT_AVAILABILITY_CHECK_FAILED` (400, consider 422).
    - Non-Feign errors map to `INTERNAL_SERVER_ERROR` (500).