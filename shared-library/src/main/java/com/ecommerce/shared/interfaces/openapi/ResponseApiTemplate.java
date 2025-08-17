package com.ecommerce.shared.interfaces.openapi;

public class ResponseApiTemplate {

    public static final String UNAUTHORIZED = """
            {
                "status": 401,
                "errorCode": "EC-001",
                "message": "Unauthorized access. Invalid or missing JWT token.",
                "details": "Invalid token provided",
                "timestamp": "2025-08-16T23:01:54.361195666Z",
                "stackTrace": []
            }
            """;
    public static final String FORBIDDEN = """
            {
                "status": 403,
                "errorCode": "EC-002",
                "message": "Forbidden. Insufficient permissions.",
                "details": "User role is not authorized for this resource",
                "timestamp": "2025-08-16T23:01:54.361195666Z",
                "stackTrace": []
            }
            """;
    public static final String RATE_LIMIT = """
            {
                "status": 429,
                "errorCode": "EC-003",
                "message": "Rate limit exceeded. Try again later.",
                "details": "Too many requests in a given amount of time.",
                "timestamp": "2025-08-16T23:01:54.361195666Z",
                "stackTrace": []
            }
            """;
    public static final String UNPROCESSABLE = """
            {
                "status": 422,
                "errorCode": "GEN-001",
                "message": "Validation error. Check 'errors' field for details.",
                "details": "",
                "timestamp": "2025-08-16T23:01:54.361195666Z",
                "stackTrace": [],
                "errors": [
                    {
                        "field": "orderId",
                        "message": "'orderId' field not should be null or empty"
                    }
                ]
            }
            """;
    public static final String NOT_FOUND = """
            {
                "status": 404,
                "errorCode": "GEN-002",
                "message": "Resource not found.",
                "details": "The requested resource does not exist.",
                "timestamp": "2025-08-16T23:01:54.361195666Z",
                "stackTrace": []
            }
            """;
    public static final String INTERNAL_SERVER_ERROR = """
            {
                  "status": 500,
                  "errorCode": "GEN-003",
                  "message": "Cannot invoke \\"java.util.List.iterator()\\" because the return value of \\"Class.getItems()\\" is null",
                  "timestamp": "2025-08-16T23:01:54.361195666Z",
                  "stackTrace": []
            }
            """;
    public static final String SERVICE_UNAVAILABLE = """
            {
                  "status": 503,
                  "errorCode": "GEN-004",
                  "message": "Service unavailable",
                  "details": "280d23221468 executing POST http://service/",
                  "timestamp": "2025-08-16T23:01:54.361195666Z",
                  "stackTrace": []
            }
            """;
}
