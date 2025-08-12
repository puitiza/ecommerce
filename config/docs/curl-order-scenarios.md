# API Curl Examples for Order Service

This document contains curl commands for Order Service-specific scenarios (e.g., validation, business rules).
Gateway-level errors (401/403/429/503/500) are in [curl-gateway-scenarios.md](curl-gateway-scenarios.md).

All examples assume:

- The service is running via gateway at `http://localhost:8090` (update for your env).
- You have a valid JWT token (replace `••••••` with your actual token).
- Requests use JSON where applicable.
- Error codes are ORD-xxx (order-specific) or GEN-xxx (general but from order context).
- For detailed error traces in dev mode (not production), add `?trace=true` to the URL.
- Most scenarios here are 400 Bad Request or 404 Not Found.

## Order Creation Scenarios

### Scenario 1: Create Order - Product Not Found

**Description**: Attempt to create an order with a non-existent product ID, expecting 404 Not Found.

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
  "status": 404,
  "errorCode": "GEN-002",
  "message": "Product with id 2 not found",
  "timestamp": "2025-08-12T16:17:54.863491508Z"
}
```

**Notes**: Tests product lookup failure. General error code: GEN-002.

### Scenario 2: Create Order - Missing Shipping Address

**Description**: Attempt to create an order without a shipping address, expecting 400 Bad Request for validation error.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
--data '{
    "items": [
      { "productId": 2, "quantity": 10 },
      { "productId": 3, "quantity": 5 }
    ]
}'
```

**Expected Response**:

```json
{
  "status": 400,
  "errorCode": "GEN-001",
  "message": "Validation error: Shipping address is required",
  "details": "Validation failed for argument [0] in public com.ecommerce.orderservice.application.dto.OrderResponse com.ecommerce.orderservice.interfaces.rest.OrderController.createOrder(com.ecommerce.orderservice.application.dto.OrderRequest): [Field error in object 'orderRequest' on field 'shippingAddress': rejected value [null]; codes [NotBlank.orderRequest.shippingAddress,NotBlank.shippingAddress,NotBlank.java.lang.String,NotBlank]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [orderRequest.shippingAddress,shippingAddress]; arguments []; default message [shippingAddress]]; default message [Shipping address is required]] ",
  "timestamp": "2025-08-12T16:20:41.350726418Z",
  "errors": [
    {
      "field": "shippingAddress",
      "message": "Shipping address is required"
    }
  ]
}
```

**Notes**: Tests request validation. General error code: GEN-001. Consider changing to 422 for semantic validation
precision.

### Scenario 3: Create Order - Duplicate Product ID

**Description**: Attempt to create an order with duplicate product IDs, expecting 400 Bad Request.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
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
  "status": 400,
  "errorCode": "ORD-002",
  "message": "Duplicate product found with id: 2 in the order",
  "timestamp": "2025-08-12T16:22:32.731326887Z"
}
```

**Notes**: Tests business rule for unique products. Order-specific error code: ORD-002. Suggest changing to 422 for
better semantic distinction from syntax errors.

## Error Handling Scenarios

### Scenario 4: Get Order by ID - Invalid UUID Format

**Description**: Attempt to get an order with an invalid UUID, expecting 400 Bad Request.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders/0' \
--header 'Authorization: ••••••'
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

**Notes**: Tests path variable validation. General error code: GEN-001. This is syntax-related, so 400 is appropriate;
no change to 422 needed.

### Scenario 5: Search Orders - Missing Required Parameter

**Description**: Attempt to search orders without the 'active' parameter, expecting 400 Bad Request.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders/search' \
--header 'Authorization: ••••••'
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

**Notes**: Tests query parameter validation. General error code: GEN-001. Syntax-related; keep as 400.

### Scenario 6: Search Orders - Invalid Path with Trace (Dev Mode)

**Description**: Attempt to search with an invalid path and enable trace for stack details, expecting 400 Bad Request.

**Curl Command**:

```bash
curl --location 'http://localhost:8090/orders/search?trace=true' \
--header 'Authorization: ••••••'
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
    "org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver.convertIfNecessary(AbstractNamedValueMethodArgumentResolver.java:301)",
    "org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver.resolveArgument(AbstractNamedValueMethodArgumentResolver.java:136)",
    "org.springframework.web.method.support.HandlerMethodArgumentResolverComposite.resolveArgument(HandlerMethodArgumentResolverComposite.java:122)",
    "org.springframework.web.method.support.InvocableHandlerMethod.getMethodArgumentValues(InvocableHandlerMethod.java:227)",
    "org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:181)",
    "org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118)",
    "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:991)",
    "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:896)",
    "org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)",
    "org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1089)",
    "org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:979)",
    "org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)",
    "org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903)",
    "jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564)"
  ]
}
```

**Notes**: Tests dev-mode tracing. General error code: GEN-001. Syntax-related; keep as 400.

## Other Endpoints

<!-- Add scenarios for GET /orders (pagination), PUT /orders/{id}, DELETE /orders/{id}, etc., as needed. For example, insufficient inventory (ORD-003), payment failures (ORD-006). -->

## Notes on Error Codes and Statuses

- Order-specific errors: Mostly 400 for validation (e.g., ORD-001/002/003/004), 404 for not found (GEN-002).
- On 400 vs 422: For semantic/business validation (e.g., duplicates ORD-002, insufficient inventory ORD-003), change to
  422 Unprocessable Entity for precision. Syntax errors (missing fields, invalid types) stay 400. Update in
  `ExceptionHandlerConfig.java`:
    - For `OrderValidationException` (business rules): Return HttpStatus.UNPROCESSABLE_ENTITY (422).
    - Keep 400 for `MethodArgumentNotValidException` (syntax/validation annotations).
- This aligns with HTTP standards: 400 for "bad syntax", 422 for "understood but invalid semantically". Implement in
  code if desired.