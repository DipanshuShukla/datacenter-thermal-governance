package com.enginpilot.ingestiongateway.service;

import com.enginpilot.ingestiongateway.dto.TelemetryRecordDTO;
import com.enginpilot.ingestiongateway.exception.TelemetryPublishingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * High-throughput service responsible for serializing telemetry events and publishing them to Kafka.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RawTelemetryPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${app.kafka.topic}")
    private String topic;

    /**
     * Serializes the telemetry record to JSON and publishes it to the designated Kafka topic.
     * Uses datacenter_id as the message key to partition data across clusters predictably.
     */
    public void publishEvent(TelemetryRecordDTO event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            // Utilizing datacenter_id as the routing key to align processing topologies
            kafkaTemplate.send(topic, event.getDatacenterId(), jsonPayload);
        } catch (JacksonException e) {
            log.error("Failed to serialize telemetry event JSON: " + e.getMessage());

            // Rethrow as an unchecked exception so the GlobalExceptionHandler catches it.
            // This prevents the API from returning a false "202 Accepted" when data is lost.
            throw new TelemetryPublishingException("Serialization failure during telemetry ingestion", e);
        }
    }
}