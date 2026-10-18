package com.enginpilot.governanceengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing raw telemetry data ingested from data center racks.
 * Shared across microservices via the common-domain Maven module.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryRecordDTO {

    @NotBlank(message = "Validation Error: rack_id is required.")
    @JsonProperty("rack_id")
    private String rackId;

    @NotBlank(message = "Validation Error: datacenter_id is required.")
    @JsonProperty("datacenter_id")
    private String datacenterId;

    @Positive(message = "Validation Error: valid timestamp is required.")
    @JsonProperty("timestamp")
    private long timestamp;

    @PositiveOrZero(message = "Validation Error: it_load_kw cannot be negative.")
    @JsonProperty("it_load_kw")
    private double itLoadKw;

    @JsonProperty("crah_supply_temp_c")
    private double crahSupplyTempC;

    @Positive(message = "Validation Error: crah_airflow_m3s must be greater than zero.")
    @JsonProperty("crah_airflow_m3s")
    private double crahAirflowM3s;

    @JsonProperty("actual_rack_temp_c")
    private double actualRackTempC;
}