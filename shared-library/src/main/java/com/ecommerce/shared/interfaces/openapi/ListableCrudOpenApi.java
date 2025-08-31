package com.ecommerce.shared.interfaces.openapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Optional interface for services that support retrieving all resources with pagination.
 * Type parameter:
 * - P: The paginated response type (e.g., OrderPageResponse, List<T>).
 */
public interface ListableCrudOpenApi<P> {

    @Operation(summary = "Retrieve All Resources", description = "Retrieves a paginated list of all resources.",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Resources retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Object.class)))
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    P findAllPaginated(@Parameter(description = "Page number for pagination", example = "0")
                       @RequestParam(defaultValue = "0") int page,
                       @Parameter(description = "Number of items per page", example = "10")
                       @RequestParam(defaultValue = "10") int size);
}
