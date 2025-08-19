package com.ecommerce.orderservice.infrastructure.adapter.security.dto;

/**
 * DTO for transferring user authentication details between the security adapter and application layer.
 */
public record UserAuthenticationDetails(String token, String userId) {}