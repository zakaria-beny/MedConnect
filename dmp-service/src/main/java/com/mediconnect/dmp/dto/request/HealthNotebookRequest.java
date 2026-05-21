package com.mediconnect.dmp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthNotebookRequest {

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
}
