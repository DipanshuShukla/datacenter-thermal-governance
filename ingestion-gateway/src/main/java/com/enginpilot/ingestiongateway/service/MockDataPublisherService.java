package com.enginpilot.ingestiongateway.service;

import com.enginpilot.ingestiongateway.dto.TelemetryRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled background service executing in local/dev profiles to generate mock telemetry.
 * Simulates a 5-minute anomaly cycle to evaluate downstream physics calculations
 * and the DESCRIBE-ONLY governance engine.
 */
@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class MockDataPublisherService {

    private final RawTelemetryPublisherService publisherService;

    // PoC Spec Constants
    private static final String RACK_ID = "RACK-01";
    private static final String DATACENTER_ID = "DC_POC";

    // Baseline and Anomaly values
    private static final double BASE_IT_LOAD_KW = 5.0;
    private static final double ANOMALY_IT_LOAD_KW = 7.0; // 40% spike at T=120s
    private static final double CRAH_SUPPLY_TEMP_C = 18.0;
//    private static final double CRAH_AIRFLOW_M3S = 2.5;
    private static final double CRAH_AIRFLOW_M3S = .5; // temporary

    // Thermal inertia simulation parameters
    private static final double ALPHA = 0.1;
    private double currentActualTempC = 24.0; // Starting baseline temp

    // 5-Minute Cycle tracking
    private int cycleTimeSeconds = 0;
    private static final int CYCLE_DURATION_SECONDS = 300;
    private static final int ANOMALY_START_SECONDS = 120;

    /**
     * Executes every 2 seconds.
     * Emits: rack_id, timestamp, it_load_kw, crah_supply_temp_c, crah_airflow_m3s, actual_rack_temp_c.
     */
    @Scheduled(fixedRate = 2000)
    public void publishMockEvents() {
        long currentTimestamp = System.currentTimeMillis();

        // Determine if we are currently inside the anomaly window (T >= 120s)
        boolean isAnomaly = cycleTimeSeconds >= ANOMALY_START_SECONDS;

        // Apply load spike
        double currentLoadKw = isAnomaly ? ANOMALY_IT_LOAD_KW : BASE_IT_LOAD_KW;

        // Internal target temperature to drive actual_rack_temp_c.
        // Spikes during the anomaly to ensure the downstream actual temp eventually
        // climbs to trigger the required T1/T2 rule thresholds.
        double targetTempC = isAnomaly ? 29.5 : 24.0;

        // Simulate sensor thermal inertia using an exponential moving average (alpha = 0.1)
        currentActualTempC = (ALPHA * targetTempC) + ((1.0 - ALPHA) * currentActualTempC);

        // Round to two decimal places for cleaner downstream visualization
        currentActualTempC = Math.round(currentActualTempC * 100.0) / 100.0;

        TelemetryRecordDTO mockRecord = TelemetryRecordDTO.builder()
                .rackId(RACK_ID)
                .datacenterId(DATACENTER_ID)
                .timestamp(currentTimestamp)
                .itLoadKw(currentLoadKw)
                .crahSupplyTempC(CRAH_SUPPLY_TEMP_C)
                .crahAirflowM3s(CRAH_AIRFLOW_M3S)
                .actualRackTempC(currentActualTempC)
                .build();

        publisherService.publishEvent(mockRecord);
        log.debug("T={}s | Published Telemetry: {}", cycleTimeSeconds, mockRecord);

        // Increment time by the 2-second polling interval
        cycleTimeSeconds += 2;

        // Reset the cycle after 5 minutes to allow for repeatable demos without container restarts
        if (cycleTimeSeconds >= CYCLE_DURATION_SECONDS) {
            cycleTimeSeconds = 0;
            currentActualTempC = 24.0; // Clean reset of the thermal sensor state
            log.info("5-minute anomaly cycle completed. Resetting T=0.");
        }
    }
}