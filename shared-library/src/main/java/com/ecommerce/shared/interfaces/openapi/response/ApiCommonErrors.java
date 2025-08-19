package com.ecommerce.shared.interfaces.openapi.response;

import com.ecommerce.shared.domain.model.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Common API error responses for general issues.
 * These responses are applicable to most, if not all, API endpoints.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ApiResponses({
        @ApiResponse(responseCode = "429", description = "Too Many Requests",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "tooManyRequests",
                                description = "Rate limit exceeded",
                                value = """
                                        {
                                          "status": 429,
                                          "errorCode": "EC-003",
                                          "message": "Too many requests.",
                                          "details": "Rate limit exceeded, please try again later",
                                          "timestamp": "2025-08-18T02:41:54.361195666Z",
                                          "stackTrace": [],
                                          "errors": null
                                        }
                                        """))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "internalServerError",
                                description = "Unexpected server error",
                                value = """
                                        {
                                          "status": 500,
                                          "errorCode": "EC-004",
                                          "message": "Internal server error.",
                                          "details": "An unexpected error occurred on the server",
                                          "timestamp": "2025-08-18T02:41:54.361195666Z",
                                          "stackTrace": [],
                                          "errors": null
                                        }
                                        """))),
        @ApiResponse(responseCode = "503", description = "Service Unavailable",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "serviceUnavailable",
                                description = "Service temporarily unavailable",
                                value = """
                                        {
                                          "status": 503,
                                          "errorCode": "EC-005",
                                          "message": "Service unavailable.",
                                          "details": "The service is temporarily down for maintenance",
                                          "timestamp": "2025-08-18T02:41:54.361195666Z",
                                          "stackTrace": [],
                                          "errors": null
                                        }
                                        """)))
})
public @interface ApiCommonErrors {
}