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

    // ─── Internal IDs ────────────────────────────────────────
    @NotBlank(message = "Patient ID is required")
    private String patientId;

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    // ─── Human-readable fields (shown in PDF) ────────────────
    @NotBlank(message = "Patient full name is required")
    private String patientFullName;

    private String patientDateOfBirth;

    @NotBlank(message = "Doctor full name is required")
    private String doctorFullName;

    private String doctorSpecialty;

    private String doctorRppsNumber;

    private String clinicName;

    private String clinicAddress;

    private String clinicPhone;

    // ─── Items ───────────────────────────────────────────────
    @NotEmpty(message = "Prescription items cannot be empty")
    @Valid
    private List<PrescriptionItemRequest> items;

    private String notes;
    private int refillsAllowed;
    private boolean controlledSubstance;
}