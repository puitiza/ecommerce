package com.ecommerce.shared.interfaces.openapi.response;

import com.ecommerce.shared.domain.model.ErrorResponse;
import com.ecommerce.shared.interfaces.openapi.ResponseApiTemplate;
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
 * Common API error responses for unauthorized, forbidden, and server issues.
 * These responses are applicable to most, if not all, API endpoints.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ResponseApiTemplate.UNAUTHORIZED))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ResponseApiTemplate.FORBIDDEN)))
})
public @interface ApiAuthErrors {
}