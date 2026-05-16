package com.mediconnect.prescription.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {
    @NotBlank(message = "Patient ID is required")
    private String patientId;

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    @NotBlank(message = "Doctor name is required")
    private String doctorName;

    @NotEmpty(message = "Prescription items cannot be empty")
    @Valid
    private List<PrescriptionItemRequest> items;

    private String notes;
    private int refillsAllowed;
    private boolean controlledSubstance;
}
