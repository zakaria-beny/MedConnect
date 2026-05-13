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
@Document(collection = "chronic_conditions")
public class ChronicCondition {

    @Id
    private String id;

    private String patientId;

    // Human-readable name (e.g., "Type 2 Diabetes")
    private String conditionName;

    /*
     * ICD-10 = International Classification of Diseases, 10th revision
     * It's the international standard coding system for diseases.
     * Example: "E11" = Type 2 Diabetes, "I10" = Hypertension
     * This is REQUIRED in professional medical software.
     */
    private String icd10Code;

    // When was the condition first diagnosed
    private LocalDate diagnosedDate;

    // Which doctor made the diagnosis
    private String diagnosedBy;

    // How severe the condition is
    private ConditionStatus status;

    // Additional clinical notes
    private String notes;

    // Specific treatment plan for this condition
    private String treatmentPlan;

    // Date of last review by doctor
    private LocalDate lastReviewedDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ConditionStatus {
        ACTIVE,       // ongoing condition being managed
        CONTROLLED,   // under control with treatment
        REMISSION,    // currently not active
        RESOLVED,     // fully cured/resolved
        CHRONIC       // permanent, lifelong
    }
}