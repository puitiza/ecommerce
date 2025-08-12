# API Curl Examples for Gateway-Level Scenarios

This document contains curl commands for gateway-level error scenarios that are shared across all microservices (e.g.,
authentication, authorization, rate limiting, service unavailability). These are handled by the API Gateway and apply to
endpoints like Order Service, Product Service, Payment Service, etc.

All examples assume:

- The gateway is at `http://localhost:8090` (update for your env).
- You have a valid/invalid JWT token (replace `••••••` with your actual token).
- Error codes are EC-xxx (gateway-specific).
- For detailed error traces in dev mode (not production), add `?trace=true` to any endpoint URL.
- These cover common HTTP statuses: 401, 403, 429, 503, 500.

## Authentication and Authorization Scenarios

### Scenario 1: Any Endpoint - Authentication Failed (Invalid/No Token)

**Description**: Attempt to access any protected endpoint (e.g., create order) without a valid auth token, expecting 401
Unauthorized.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
--data '{
    "items": [
      { "productId": 2, "quantity": 10 },
      { "productId": 3, "quantity": 5 }
    ],
    "shippingAddress": "123 Main St, Anytown, CA 12345"
}'
```

**Expected Response Variants**:

```json
{
  "status": 401,
  "errorCode": "EC-001",
  "message": "Authentication Failed",
  "details": "Failed to validate the token",
  "timestamp": "2025-08-12T15:22:42.271842751Z"
}
```

```json
{
  "status": 401,
  "errorCode": "EC-001",
  "message": "Authentication Failed",
  "details": "Jwt expired at 2025-08-12T15:33:23Z",
  "timestamp": "2025-08-12T16:15:15.680944837Z"
}
```

```json
{
  "status": 401,
  "errorCode": "EC-001",
  "message": "Authentication Failed",
  "details": "An error occurred while attempting to decode the Jwt: Invalid JWT serialization: Missing dot delimiter(s)",
  "timestamp": "2025-08-12T16:16:50.741998548Z"
}
```

**Notes**: Tests OAuth2/JWT validation. Gateway error code: EC-001. Applies to all services.

### Scenario 2: Any Endpoint - Access Denied (Insufficient Role)

**Description**: Attempt to access any endpoint (e.g., get orders) with a token that has the wrong role (e.g., '
ROLE_admin' instead of 'User'), expecting 403 Forbidden.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Authorization: ••••••'
```

**Expected Response**:

```json
{
  "status": 403,
  "errorCode": "EC-002",
  "message": "Access Denied",
  "details": "Access denied to /orders (GET). User roles: [ROLE_admin]. Required role: User",
  "timestamp": "2025-08-12T15:28:27.394172841Z"
}
```

**Notes**: Verifies role-based access control (RBAC). Gateway error code: EC-002. Applies to all services.

## Error Handling Scenarios

### Scenario 3: Any Endpoint - Service Unavailable

**Description**: Attempt to access an endpoint when a dependent service (e.g., product) is down, expecting 503 Service
Unavailable.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
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
  "status": 503,
  "errorCode": "GEN-004",
  "message": "Product service unavailable for Id 2",
  "details": "0764a14f7ec2 executing POST http://product-service/products/verify-availability",
  "timestamp": "2025-08-12T16:23:38.67522325Z"
}
```

**Notes**: Tests circuit breaker or external failure. General error code: GEN-004 (proxied via gateway).

### Scenario 4: Any Endpoint - Unexpected Internal Error

**Description**: Trigger an internal server error (e.g., null reference), expecting 500 Internal Server Error.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ****' \
--data '{
    "shippingAddress": "123 Main St, Anytown, CA 12345"
  }'
```

**Expected Response**:

```json
{
  "status": 500,
  "message": "Cannot invoke \"java.util.List.iterator()\" because the return value of \"com.ecommerce.orderservice.model.request.CreateOrderRequest.getItems()\" is null",
  "errorCode": "GEN-003",
  "timestamp": "2025-07-21T12:44:15.267947-05:00"
}
```

**Notes**: Tests unexpected errors. General error code: GEN-003. Handled via gateway as EC-004 if unexpected.

### Scenario 5: Any Endpoint - Rate Limit Exceeded

**Description**: Exceed API rate limits on any endpoint, expecting 429 Too Many Requests. (Simulate by rapid requests.)

**Curl Command**:

```bash
# Repeat this curl multiple times quickly
curl --location 'http://localhost:8090/orders' \
--header 'Authorization: ••••••'
```

**Expected Response**:

```json
{
  "status": 429,
  "errorCode": "EC-003",
  "message": "Rate Limit exceeded",
  "timestamp": "2025-08-12T16:00:00Z"
}
```

**Notes**: Tests rate limiting. Gateway error code: EC-003. Applies to all services.