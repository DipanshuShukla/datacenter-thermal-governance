package com.enginpilot.ingestiongateway.controller;

import com.enginpilot.ingestiongateway.dto.TelemetryRecordDTO;
import com.enginpilot.ingestiongateway.service.TelemetryIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Controller for external data center telemetry ingestion.
 * Acts as the HTTP-to-Kafka bridge for the ENGINPILOT-DCIP platform.
 */
@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
@Slf4j
public class TelemetryIngestionController {

    private final TelemetryIngestionService ingestionService;

    /**
     * Ingests a raw telemetry payload from a data center rack.
     * The @Valid annotation automatically triggers the constraints defined in TelemetryRecordDTO.
     * If validation fails, the GlobalExceptionHandler will automatically return a 400 Bad Request.
     *
     * @param telemetryRecord The incoming JSON payload matching TelemetryRecordDTO.
     * @return HTTP 202 Accepted if the payload passes validation and is queued for Kafka.
     */
    @PostMapping
    public ResponseEntity<Void> ingestTelemetry(@Valid @RequestBody TelemetryRecordDTO telemetryRecord) {
        log.debug("Received telemetry ingestion request for Rack: {}", telemetryRecord.getRackId());

        // Delegate to the service layer for business orchestration
        ingestionService.processTelemetry(telemetryRecord);

        // Return HTTP 202 Accepted - acknowledging receipt for async downstream processing
        return ResponseEntity.accepted().build();
    }
}