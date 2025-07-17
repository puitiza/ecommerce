package com.ecommerce.productservice.configuration.exception;

import com.ecommerce.productservice.configuration.exception.handler.InvalidInventoryException;
import com.ecommerce.productservice.configuration.exception.handler.NoSuchElementFoundException;
import com.ecommerce.productservice.configuration.exception.handler.ProductUpdateException;
import com.ecommerce.shared.exception.BuildErrorResponse;
import com.ecommerce.shared.exception.GlobalErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerConfig extends ResponseEntityExceptionHandler {

    private final BuildErrorResponse buildErrorResponse;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        log.error("Validation error for request:", ex);
        GlobalErrorResponse errorResponse = new GlobalErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Validation error. Check 'errors' field for details.", "PROD-001");
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        buildErrorResponse.addTrace(errorResponse, ex, buildErrorResponse.shouldIncludeStackTrace(request));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler({NoSuchElementFoundException.class})
    public ResponseEntity<Object> handleNoSuchElementFoundException(Exception ex, WebRequest request) {
        log.error("Failed to find the requested element", ex);
        return buildErrorResponse.structure(ex, HttpStatus.NOT_FOUND, request,"PROD-002");
    }

    @ExceptionHandler({InvalidInventoryException.class})
    public ResponseEntity<Object> handleInvalidInventoryException(Exception ex, WebRequest request) {
        log.error("Invalid inventory value provided", ex);
        return buildErrorResponse.structure(ex, HttpStatus.BAD_REQUEST, request,"PROD-003");
    }

    @ExceptionHandler({ProductUpdateException.class})
    public ResponseEntity<Object> handleProductUpdateException(Exception ex, WebRequest request) {
        log.error("Failed to update product inventory", ex);
        return buildErrorResponse.structure(ex, HttpStatus.INTERNAL_SERVER_ERROR, request,"PROD-004");
    }

}
