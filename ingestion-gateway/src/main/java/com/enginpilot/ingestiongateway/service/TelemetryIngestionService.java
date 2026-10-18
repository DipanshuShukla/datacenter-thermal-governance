package com.enginpilot.ingestiongateway.service;

import com.enginpilot.ingestiongateway.dto.TelemetryRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business orchestration service for telemetry ingestion.
 * Sits between the HTTP boundary and the messaging infrastructure.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryIngestionService {

    private final RawTelemetryPublisherService publisherService;

    /**
     * Processes validated telemetry and publishes it to the event stream.
     * Any complex, cross-field business logic or database enrichment would occur here.
     *
     * @param telemetryRecord The pre-validated telemetry record.
     */
    public void processTelemetry(TelemetryRecordDTO telemetryRecord) {

        publisherService.publishEvent(telemetryRecord);

        log.debug("Telemetry successfully processed and routed to Kafka for Rack: {}", telemetryRecord.getRackId());
    }
}