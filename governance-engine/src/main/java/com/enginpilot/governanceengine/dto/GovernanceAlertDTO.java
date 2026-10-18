package com.enginpilot.governanceengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceAlertDTO {
    private String rackId;
    private String severity; // "NORMAL", "T1_ADVISORY", "T2_CRITICAL"
    private String message;
    private double tPredictedC;
    private double tActualC;
    private long timestamp;
}