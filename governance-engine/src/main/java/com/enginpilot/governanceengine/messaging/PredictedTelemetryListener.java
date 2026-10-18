package com.enginpilot.governanceengine.messaging;

import com.enginpilot.governanceengine.dto.PredictedTelemetryDTO;
import com.enginpilot.governanceengine.service.GovernanceRulesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PredictedTelemetryListener {

    private final GovernanceRulesService governanceService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topic.predicted}", groupId = "governance-engine-group")
    public void consumePredictedTelemetry(String message) {
        try {
            PredictedTelemetryDTO telemetry = objectMapper.readValue(message, PredictedTelemetryDTO.class);
            governanceService.evaluate(telemetry);
        } catch (Exception e) {
            log.error("Failed to process predicted telemetry in Governance Engine", e);
        }
    }
}