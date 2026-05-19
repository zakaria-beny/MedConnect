package com.mediconnect.prescription.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrescriptionResponse {

    private String id;

    // ─── IDs ─────────────────────────────────────────────────
    private String patientId;
    private String doctorId;

    // ─── Patient info (shown in PDF / UI) ────────────────────
    private String patientFullName;        // ← was missing
    private String patientDateOfBirth;     // ← was missing

    // ─── Doctor info ─────────────────────────────────────────
    private String doctorFullName;         // ← was missing (replaces doctorName)
    private String doctorSpecialty;        // ← was missing
    private String doctorRppsNumber;       // ← was missing

    // ─── Clinic info ─────────────────────────────────────────
    private String clinicName;             // ← was missing
    private String clinicAddress;          // ← was missing
    private String clinicPhone;            // ← was missing

    // ─── Prescription content ────────────────────────────────
    private List<PrescriptionItemResponse> items;
    private String notes;
    private int refillsAllowed;
    private int refillsUsed;
    private boolean controlledSubstance;

    // ─── Status & workflow ───────────────────────────────────
    private String status;
    private String digitalSignature;
    private LocalDateTime signedAt;
    private String qrCodeBase64;
    private String pharmacyId;
    private LocalDateTime sentToPharmacyAt;
    private LocalDateTime dispensedAt;
    private String dispensedByPharmacistId;

    // ─── Dates ───────────────────────────────────────────────
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}