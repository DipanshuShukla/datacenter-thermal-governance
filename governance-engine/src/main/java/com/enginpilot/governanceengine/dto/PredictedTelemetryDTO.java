package com.enginpilot.governanceengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enriched Data Transfer Object containing both the raw telemetry and the calculated
 * physics predictions from the THERM-NET engine.
 * Published to the 'telemetry_predicted' Kafka topic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictedTelemetryDTO {

    // The @Valid annotation ensures that if this DTO is ever passed into a Spring controller,
    // it will recursively validate the nested raw telemetry data.
    @Valid
    @NotNull(message = "Validation Error: raw_telemetry cannot be null in predicted payload.")
    @JsonProperty("raw_telemetry")
    private TelemetryRecordDTO rawTelemetry;

    @JsonProperty("t_predicted_c")
    private double tPredictedC;

    @JsonProperty("temp_delta")
    private double tempDelta; // Divergence: T_predicted - T_actual

    @JsonProperty("rho_kg_m3")
    private double rhoKgM3;

    @JsonProperty("cp_j_kgk")
    private double cpJKgK;

    @JsonProperty("calc_latency_ms")
    private long calcLatencyMs;

    // Flag indicating if the CoolProp C++ binary was successfully used,
    // or if the engine had to fall back to analytical constants.
    @JsonProperty("used_coolprop")
    private boolean usedCoolprop;
}