package com.ecommerce.shared.domain.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends ServiceException {
    public ResourceNotFoundException(String resourceType, String id) {
        super(ExceptionError.NOT_FOUND, null, resourceType, id);
    }
}