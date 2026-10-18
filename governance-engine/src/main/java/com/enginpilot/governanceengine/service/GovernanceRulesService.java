package com.enginpilot.governanceengine.service;

import com.enginpilot.governanceengine.dto.DigitalTwinStateDTO;
import com.enginpilot.governanceengine.dto.GovernanceAlertDTO;
import com.enginpilot.governanceengine.dto.PredictedTelemetryDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class GovernanceRulesService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String REDIS_STATE_PREFIX = "twin:state:";
    private static final String REDIS_STRIKE_T1_PREFIX = "twin:strikes:t1:";
    private static final String REDIS_STRIKE_T2_PREFIX = "twin:strikes:t2:";

    @Value("${app.kafka.topic.alert:governance_alerts}")
    private String alertTopic;

    public void evaluate(PredictedTelemetryDTO telemetry) {
        String rackId = telemetry.getRawTelemetry().getRackId();
        double tPredicted = telemetry.getTPredictedC();
        double tActual = telemetry.getRawTelemetry().getActualRackTempC();

        log.debug("Evaluating governance rules for Rack: {} | T_pred: {} | T_actual: {}", rackId, tPredicted, tActual);

        GovernanceAlertDTO alert = null;

        String t1Key = REDIS_STRIKE_T1_PREFIX + rackId;
        String t2Key = REDIS_STRIKE_T2_PREFIX + rackId;

        // Rule 2: T2 Critical (Evaluated first for priority)
        if (tPredicted > 28.0) {
            // Atomic increment directly in Redis ensures cross-pod state consistency
            Long strikes = redisTemplate.opsForValue().increment(t2Key);
            // Set a TTL so stale strikes eventually clear out if telemetry stops
            redisTemplate.expire(t2Key, Duration.ofMinutes(10));

            log.debug("Rack {} exceeded T2 threshold (28.0). T2 Strike Count: {}", rackId, strikes);

            if (strikes != null && strikes >= 3) {
                alert = buildAlert(rackId, "T2_CRITICAL", "Immediate cooling intervention required - physics model breach", tPredicted, tActual);
                log.debug("Rack {} triggered T2_CRITICAL alert condition.", rackId);
            }
        } else {
            resetStrikeCountIfActive(t2Key, rackId, "T2");
        }

        // Rule 1: T1 Advisory
        if (alert == null && tPredicted > 26.0 && tActual < 26.0) {
            Long strikes = redisTemplate.opsForValue().increment(t1Key);
            redisTemplate.expire(t1Key, Duration.ofMinutes(10));

            log.debug("Rack {} exceeded T1 threshold (26.0). T1 Strike Count: {}", rackId, strikes);

            if (strikes != null && strikes >= 3) {
                alert = buildAlert(rackId, "T1_ADVISORY", "Predicted thermal threshold approach - review cooling setpoints", tPredicted, tActual);
                log.debug("Rack {} triggered T1_ADVISORY alert condition.", rackId);
            }
        } else if (alert == null) {
            resetStrikeCountIfActive(t1Key, rackId, "T1");
        }

        // If no rules triggered, system is NORMAL
        if (alert == null) {
            alert = buildAlert(rackId, "NORMAL", "System operating within normal parameters", tPredicted, tActual);
        }

        processOutcome(telemetry, alert);
    }

    /**
     * Checks if strikes exist before deleting to avoid unnecessary Redis calls and log spam.
     */
    private void resetStrikeCountIfActive(String redisKey, String rackId, String ruleName) {
        String existingStrikes = redisTemplate.opsForValue().get(redisKey);
        if (existingStrikes != null && Integer.parseInt(existingStrikes) > 0) {
            log.debug("Rack {} {} condition broken. Resetting {} Strike Count to 0.", rackId, ruleName, ruleName);
            redisTemplate.delete(redisKey);
        }
    }

    private void processOutcome(PredictedTelemetryDTO telemetry, GovernanceAlertDTO alert) {
        try {
            String rackId = telemetry.getRawTelemetry().getRackId();

            // 1. Publish Alert to Kafka
            if (!alert.getSeverity().equals("NORMAL")) {
                kafkaTemplate.send(alertTopic, rackId, objectMapper.writeValueAsString(alert));
                log.warn("DESCRIBE-ONLY ALERT FIRED: {} for Rack {}", alert.getSeverity(), rackId);
            }

            // 2. Update Redis Digital Twin State
            DigitalTwinStateDTO state = new DigitalTwinStateDTO(telemetry, alert);
            redisTemplate.opsForValue().set(REDIS_STATE_PREFIX + rackId, objectMapper.writeValueAsString(state));

            log.debug("Successfully updated Digital Twin State in Redis for Rack: {}", rackId);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize governance outcome for Rack: {}", telemetry.getRawTelemetry().getRackId(), e);
        }
    }

    private GovernanceAlertDTO buildAlert(String rackId, String severity, String msg, double tPred, double tAct) {
        return GovernanceAlertDTO.builder()
                .rackId(rackId).severity(severity).message(msg)
                .tPredictedC(tPred).tActualC(tAct).timestamp(System.currentTimeMillis())
                .build();
    }
}