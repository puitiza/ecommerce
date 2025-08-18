package com.ecommerce.shared.interfaces.openapi;

/**
 * Provides JSON templates for OpenAPI error response examples.
 */
public class ResponseApiTemplate {

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
}
