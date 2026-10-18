package com.enginpilot.ingestiongateway.exception;

/**
 * Custom exception to indicate a failure in the messaging infrastructure layer
 * (e.g., serialization failure, Kafka broker unreachable).
 */
public class TelemetryPublishingException extends RuntimeException {
    public TelemetryPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}