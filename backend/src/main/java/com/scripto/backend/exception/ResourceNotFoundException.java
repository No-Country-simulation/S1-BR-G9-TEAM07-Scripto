package com.scripto.backend.exception;

public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(String.format("%s não encontrado com id: %s", resourceName, resourceId));
    }
    
    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super(String.format("%s não encontrado com %s: %s", resourceName, field, value));
    }
}
