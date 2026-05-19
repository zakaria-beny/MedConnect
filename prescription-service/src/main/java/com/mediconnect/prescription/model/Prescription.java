package com.mediconnect.prescription.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "prescriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Prescription {

    @Id
    private String id;

    // ─── Internal IDs (stored in DB, never shown in PDF) ───
    private String patientId;
    private String doctorId;

    // ─── Human-readable fields (shown in PDF) ───────────────
    private String patientFullName;
    private String patientDateOfBirth;  // dd/MM/yyyy
    private String doctorFullName;
    private String doctorSpecialty;
    private String doctorRppsNumber;    // French doctor license number
    private String clinicName;
    private String clinicAddress;
    private String clinicPhone;

    private List<PrescriptionItem> items;
    private PrescriptionStatus status;

    // ─── Signature (internal token, never shown in PDF) ─────
    private String digitalSignature;
    private String signatureImage; //for drawing signature
    private LocalDateTime signedAt;

    // ─── QR Code ────────────────────────────────────────────
    private String qrCodeBase64;

    // ─── Pharmacy ───────────────────────────────────────────
    private String pharmacyId;
    private LocalDateTime sentToPharmacyAt;
    private LocalDateTime dispensedAt;
    private String dispensedByPharmacistId;

    // ─── Refills ────────────────────────────────────────────
    private int refillsAllowed;
    private int refillsUsed;
    private boolean controlledSubstance;

    private String notes;
    private LocalDateTime expiresAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum PrescriptionStatus {
        DRAFT, SIGNED, SENT, DISPENSED, EXPIRED, CANCELLED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PrescriptionItem {
        private String drugName;
        private String activeIngredient;
        private String dosage;
        private String frequency;
        private String route;
        private int durationDays;
        private String instructions;
        private int quantity;
        private boolean substitutionAllowed;
    }
}