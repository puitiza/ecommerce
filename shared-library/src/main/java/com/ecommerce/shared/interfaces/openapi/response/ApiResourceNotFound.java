package com.ecommerce.shared.interfaces.openapi.response;

import com.ecommerce.shared.domain.model.ErrorResponse;
import com.ecommerce.shared.interfaces.openapi.ResponseApiTemplate;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API response for a resource not found error.
 * Applicable to methods retrieving, updating, or deleting a resource by ID.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(value = ResponseApiTemplate.NOT_FOUND)))
public @interface ApiResourceNotFound {
}