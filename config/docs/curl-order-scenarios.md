# API Curl Examples for Order Service

This document contains curl commands for Order Service-specific scenarios (e.g., validation, business rules).
Gateway-level errors (401/403/429/503/500) are in [curl-gateway-scenarios.md](curl-gateway-scenarios.md).

## Assumptions

- **Base URL**: The service runs via a gateway at `http://localhost:8090` (update for your environment).
- **Authentication**: Replace `••••••` with a valid JWT token in the `Authorization` header.
- **Content Type**: Requests use `application/json` where applicable.
- **Error Codes**:
    - `ORD-xxx`: Order-specific errors (e.g., `ORD-001` for invalid order data, `ORD-002` for duplicate products,
      `ORD-004` for product availability issues).
    - `GEN-xxx`: General errors (e.g., `GEN-001` for validation, `GEN-002` for not found).
- **Tracing**: Add `?trace=true` for detailed stack traces in development mode (not recommended for production).
- **HTTP Statuses**:
    - 400: Syntax errors (e.g., invalid JSON, missing fields).
    - 422: Semantic/business rule violations (e.g., duplicate products, product not found).
    - 404: Resource not found.

## Order Creation Scenarios

### Scenario 1: Create Order - Product Not Found (Multiple Products)

**Description**: Attempt to create an order with multiple non-existent product IDs, expecting 422 Unprocessable Entity.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ••••••' \
--data '{
    "items": [
      { "productId": 2, "quantity": 6 },
      { "productId": 40, "quantity": 1 }
    ],
    "shippingAddress": "123 Main St, Anytown, CA 12345"
}'
```

**Expected Response**:

```json
{
  "status": 422,
  "errorCode": "ORD-004",
  "message": "Could not verify product availability for productId: 2,40",
  "details": "Product ID 2: Product not found; Product ID 40: Product not found",
  "timestamp": "2025-08-17T21:44:52.967880971Z"
}
```

**Notes**:

- Tests product lookup failure for multiple products.
- Uses `ORD-004` (`ORDER_PRODUCT_AVAILABILITY_CHECK_FAILED`) for consistency.
- HTTP 422 is used for semantic errors (product not found).

### Scenario 2: Create Order - Product Not Found (Single Product)

**Description**: Attempt to create an order with one non-existent product ID, expecting 422 Unprocessable Entity.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ••••••' \
--data '{
    "items": [
      { "productId": 2, "quantity": 6 },
      { "productId": 40, "quantity": 1 }
    ],
    "shippingAddress": "123 Main St, Anytown, CA 12345"
}'
```

**Expected Response**:

```json
{
  "status": 422,
  "errorCode": "ORD-004",
  "message": "Could not verify product availability for productId: 40",
  "details": "Product ID 40: Product not found",
  "timestamp": "2025-08-18T14:57:14.01312388Z"
}
```

**Notes**:

- Tests product lookup failure for a single product.
- Uses `ORD-004` for consistency.
- HTTP 422 distinguishes semantic errors from syntax errors.

### Scenario 3: Create Order - Malformed JSON

**Description**: Attempt to create an order with invalid JSON, expecting 400 Bad Request.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ••••••' \
--data '{
    "items": [
      { "productId": 2, "quantity": 6 },
      { "productId": '
}'
```

**Expected Response**:

```json
{
  "status": 400,
  "errorCode": "GEN-001",
  "message": "Validation error: {0}",
  "details": "Invalid JSON format: JSON parse error: Unexpected end-of-input within/between Object entries",
  "timestamp": "2025-08-19T16:58:47.797368428Z"
}
```

**Notes**:

- Tests JSON parsing validation.
- Error code `GEN-001` indicates a syntactic error.
- HTTP 400 is appropriate for syntax issues.

### Scenario 4: Create Order - Missing Shipping Address

**Description**: Attempt to create an order without a shipping address, expecting 400 Bad Request.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders?trace=true' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ••••••' \
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
    "org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor.resolveArgument(...)",
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

- Tests JSON schema validation.
- Error code `GEN-001` for syntactic validation failure.
- HTTP 400 is appropriate.

### Scenario 5: Create Order - Duplicate Product ID

**Description**: Attempt to create an order with duplicate product IDs, expecting 422 Unprocessable Entity.

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

- Error code `ORD-002` for duplicate product validation.
- HTTP 422 for semantic/business rule violation.

### Scenario 6: Get Order by ID - Invalid UUID Format

**Description**: Attempt to retrieve an order with an invalid UUID format, expecting 400 Bad Request.

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
- Error code `GEN-001` for syntactic validation error.
- HTTP 400 is appropriate.

### Scenario 7: Search Orders - Missing Required Parameter

**Description**: Attempt to search orders without the required `active` query parameter, expecting 400 Bad Request.

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
- Error code `GEN-001` for syntactic validation failure.
- HTTP 400 is appropriate.

### Scenario 8: Get Paginated Orders

**Description**: Retrieve paginated orders with valid parameters, expecting 200 OK.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders?page=0&size=10&active=true' \
--header 'Authorization: Bearer ••••••'
```

**Expected Response**:

```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "status": "CREATED",
      "items": [
        {
          "productId": 1,
          "quantity": 5
        }
      ],
      "shippingAddress": "123 Main St, Anytown, CA 12345"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 1,
  "totalPages": 1
}
```

**Notes**:

- Tests pagination with `page`, `size`, and `active` parameters.
- Returns `OrderPageResponse` with paginated data.

### Scenario 9: Update Order - Invalid Status Transition

**Description**: Attempt to update an order to an invalid status, expecting 422 Unprocessable Entity.

**Curl Command**:

```bash
curl --location --request PUT 'http://localhost:8090/orders/123e4567-e89b-12d3-a456-426614174000' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ••••••' \
--data '{
    "status": "FULFILLED",
    "items": [
      { "productId": 1, "quantity": 5 }
    ],
    "shippingAddress": "123 Main St, Anytown, CA 12345"
}'
```

**Expected Response**:

```json
{
  "status": 422,
  "errorCode": "ORD-003",
  "message": "Invalid status transition for order 123e4567-e89b-12d3-a456-426614174000",
  "details": "Cannot transition from CREATED to FULFILLED",
  "timestamp": "2025-08-19T17:00:00.123456789Z"
}
```

**Notes**:

- Tests state machine validation (e.g., invalid transition via `OrderStateMachineConfig`).
- Error code `ORD-003` for invalid status transitions.
- HTTP 422 for semantic errors.

### Scenario 10: Cancel Order - Order Not Found

**Description**: Attempt to cancel a non-existent order, expecting 404 Not Found.

**Curl Command**:

```bash
curl --location --request DELETE 'http://localhost:8090/orders/999e9999-e89b-12d3-a456-426614174999' \
--header 'Authorization: Bearer ••••••'
```

**Expected Response**:

```json
{
  "status": 404,
  "errorCode": "GEN-002",
  "message": "Order with id 999e9999-e89b-12d3-a456-426614174999 not found",
  "timestamp": "2025-08-19T17:05:00.123456789Z"
}
```

**Notes**:

- Tests resource not found for order cancellation.
- Error code `GEN-002` for not found errors.
- HTTP 404 is appropriate.

## Notes on Error Codes and Statuses

- **Order-Specific Errors** (`ORD-xxx`):
    - `ORD-001`: Invalid order data (400, consider 422 for product not found).
    - `ORD-002`: Duplicate product IDs (422).
    - `ORD-003`: Invalid status transition (422).
    - `ORD-004`: Product availability check failed (422).
- **General Errors** (`GEN-xxx`):
    - `GEN-001`: Syntactic validation errors (400).
    - `GEN-002`: Resource not found (404).
- **HTTP Status Recommendations**:
    - 400: Syntax errors (invalid JSON, missing fields).
    - 422: Semantic/business rule violations (product not found, duplicate products, invalid transitions).
    - 404: Resource not found.
- **Feign Exception Handling**:
    - `ProductFeignAdapter` maps Feign 503/-1 to `SERVICE_UNAVAILABLE` (consider 503 instead of 400).
    - Other Feign 4xx/5xx errors map to `ORD-004` (422).
    - Non-Feign errors map to `INTERNAL_SERVER_ERROR` (500).

## Testing Instructions

- **Run Services**:
  ```bash
  cd api-gateway && gradlew bootRun
  cd order-service && gradlew bootRun
  ```
- **Access Swagger UI**:
    - Gateway: `http://localhost:8090/swagger-ui.html`
    - Order Service: `http://localhost:8090/orders/swagger-ui.html`
- **Test Scenarios**:
    - Replace `••••••` with a valid JWT token from Keycloak.
    - Use tools like Postman or curl to execute the commands.
    - Verify error codes, messages, and HTTP statuses match expected responses.
