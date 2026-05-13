package com.mediconnect.dmp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthNotebookResponse {
    private String id;
    private String patientId;
    private LocalDateTime measuredAt;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Integer heartRate;
    private Double bloodGlucose;
    private String bloodGlucoseUnit;
    private Double weight;
    private Double height;
    private Double temperature;
    private Integer oxygenSaturation;
    private Integer steps;
    private String patientNotes;
    private boolean flagged;
    private String alertMessage;
    private LocalDateTime createdAt;
}