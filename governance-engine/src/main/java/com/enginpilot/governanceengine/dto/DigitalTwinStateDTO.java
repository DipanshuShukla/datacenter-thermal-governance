package com.enginpilot.governanceengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the fully hydrated state of the digital twin at a specific timestamp.
 * Flattened for direct consumption by the frontend dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DigitalTwinStateDTO {
    private PredictedTelemetryDTO telemetry;
    private GovernanceAlertDTO currentStatus;
}