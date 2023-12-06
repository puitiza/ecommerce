package com.ecommerce.authservice.component.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public interface BuildStructureBody {
    ResponseEntity<Object> structure(Exception exception, String message, HttpStatus httpStatus, WebRequest request);

    ResponseEntity<Object> structure(Exception exception, HttpStatus httpStatus, WebRequest request);
}
