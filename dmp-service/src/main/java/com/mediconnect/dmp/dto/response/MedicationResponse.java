package com.mediconnect.dmp.dto.response;

import com.mediconnect.dmp.model.Medication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationResponse {
    private String id;
    private String patientId;
    private String drugName;
    private String activeIngredient;
    private String dosage;
    private String frequency;
    private Medication.RouteOfAdministration route;
    private LocalDate startDate;
    private LocalDate endDate;
    private String prescribedBy;
    private String prescriptionId;
    private String indication;
    private int refillsRemaining;
    private LocalDate lastRefillDate;
    private Medication.MedicationStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}