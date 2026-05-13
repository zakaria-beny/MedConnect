package com.mediconnect.dmp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "medications")
public class Medication {

    @Id
    private String id;

    private String patientId;

    // Drug name (e.g., "Amoxicillin")
    private String drugName;

    // Active ingredient (e.g., "amoxicillin trihydrate")
    private String activeIngredient;

    // How much per dose (e.g., "500mg")
    private String dosage;

    // How often (e.g., "3 times daily", "once at night")
    private String frequency;

    // Route of administration
    private RouteOfAdministration route;

    // When the patient started this medication
    private LocalDate startDate;

    // When the prescription ends (null = ongoing)
    private LocalDate endDate;

    // Which doctor prescribed this
    private String prescribedBy;

    // Reference to the prescription document
    private String prescriptionId;

    // Why this medication was prescribed
    private String indication;

    // How many refills are remaining
    @Builder.Default
    private int refillsRemaining = 0;

    // Date of last refill
    private LocalDate lastRefillDate;

    // Current status of this medication
    @Builder.Default
    private MedicationStatus status = MedicationStatus.ACTIVE;

    // Notes from the doctor or pharmacist
    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum RouteOfAdministration {
        ORAL,        // swallowed
        TOPICAL,     // applied to skin
        INJECTABLE,  // injection
        INHALATION,  // inhaled
        SUBLINGUAL,  // under tongue
        OPHTHALMIC,  // eye drops
        OTIC,        // ear drops
        NASAL        // nasal spray
    }

    public enum MedicationStatus {
        ACTIVE,      // currently taking
        STOPPED,     // stopped by doctor
        COMPLETED,   // full course completed
        SUSPENDED    // temporarily paused
    }
}