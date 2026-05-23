package com.mediconnect.dmp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "allergies")
public class Allergy {

    // @Id = this field maps to MongoDB's _id field (auto-generated)
    @Id
    private String id;

    // Which patient this allergy belongs to
    private String patientId;

    // The substance the patient is allergic to (e.g., "Penicillin", "Peanuts")
    private String allergen;

    // How severe the allergy is
    private SeverityLevel severity;

    // What happens when exposed (e.g., "Anaphylaxis", "Hives")
    private String reaction;

    // Additional notes from the doctor
    private String notes;

    // Is this allergy still active or historical?
    @Builder.Default
    private boolean active = true;

    // Automatically set when the document is first saved
    @CreatedDate
    private LocalDateTime createdAt;

    // Automatically updated every time the document is saved
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /*
     * Enum = a fixed list of allowed values.
     * Instead of storing "very bad" or "mild", we use standardized codes.
     * This prevents typos and makes querying reliable.
     */
    public enum SeverityLevel {
        LIFE_THREATENING,  // anaphylaxis - ER immediately
        SEVERE,            // serious reaction
        MODERATE,          // uncomfortable but manageable
        MILD               // minor reaction
    }
}