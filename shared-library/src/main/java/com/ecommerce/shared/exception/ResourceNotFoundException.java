package com.ecommerce.shared.exception;

public class ResourceNotFoundException extends ServiceException {
    public ResourceNotFoundException(String resourceType, String id) {
        super(ExceptionError.NOT_FOUND, null, resourceType, id);
    }
}