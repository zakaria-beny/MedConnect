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

    private String patientId;
    private String doctorId;
    private String doctorName;
    private List<PrescriptionItem> items;
    private PrescriptionStatus status;
    private String digitalSignature;
    private LocalDateTime signedAt;
    private String qrCodeBase64;
    private String pharmacyId;
    private LocalDateTime sentToPharmacyAt;
    private LocalDateTime dispensedAt;
    private String dispensedByPharmacistId;
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
