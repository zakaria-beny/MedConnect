package com.mediconnect.dmp.dto.request;

import com.mediconnect.dmp.model.Medication;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequest {

    @NotBlank(message = "Drug name is required")
    private String drugName;

    private String activeIngredient;

    @NotBlank(message = "Dosage is required")
    private String dosage;

    @NotBlank(message = "Frequency is required")
    private String frequency;

    @NotNull(message = "Route of administration is required")
    private Medication.RouteOfAdministration route;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;         // null = ongoing
    private String prescribedBy;
    private String prescriptionId;
    private String indication;
    private int refillsRemaining;
    private String notes;
}