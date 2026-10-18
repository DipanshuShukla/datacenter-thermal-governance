package com.enginpilot.ingestiongateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Ingestion Gateway API.
 * Intercepts exceptions thrown by controllers or services and translates them
 * into standardized, client-friendly HTTP responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Intercepts DTO validation failures triggered by the @Valid annotation.
     * Parses the field errors and returns a structured map of the exact issues.
     *
     * @param ex The exception containing the validation errors.
     * @return 400 Bad Request with a JSON map of invalid fields and their error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Telemetry validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Intercepts custom business validation exceptions thrown manually from the service layer.
     *
     * @param ex The runtime exception.
     * @return 400 Bad Request with the text message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentExceptions(IllegalArgumentException ex) {
        log.warn("Business validation failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Intercepts failures specifically related to the Kafka messaging layer or serialization.
     *
     * @param ex The infrastructure exception.
     * @return 500 Internal Server Error with a tailored, safe message.
     */
    @ExceptionHandler(TelemetryPublishingException.class)
    public ResponseEntity<String> handleTelemetryPublishingExceptions(TelemetryPublishingException ex) {
        // We already logged the stack trace in the service, so we just log a warning here
        log.warn("Telemetry publishing failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to queue telemetry for processing. Please retry.");
    }

    /**
     * Catch-all handler for unexpected system errors (e.g., Kafka downtime, serialization issues).
     * Ensures the API never leaks stack traces to the client.
     *
     * @param ex The unexpected exception.
     * @return 500 Internal Server Error with a generic message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericExceptions(Exception ex) {
        log.error("An unexpected internal system error occurred", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An internal error occurred while processing the telemetry payload.");
    }
}