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
@Document(collection = "vaccinations")
public class Vaccination {

    @Id
    private String id;

    private String patientId;

    // Name of the vaccine (e.g., "Influenza", "COVID-19 Pfizer")
    private String vaccineName;

    // Which disease it protects against
    private String targetDisease;

    // Date the vaccine was administered
    private LocalDate administrationDate;

    // Unique batch number of the vaccine vial (for recalls)
    private String lotNumber;

    // Manufacturer name
    private String manufacturer;

    // Which dose in a series (1st, 2nd, booster)
    private int doseNumber;

    // Total doses required for full protection
    private Integer totalDosesRequired;

    // When the next dose/booster is due
    private LocalDate nextDoseDate;

    // Where it was given (clinic name or hospital)
    private String administeredAt;

    // Doctor or nurse who gave the vaccine
    private String administeredBy;

    // Any adverse reactions noted
    private String adverseReactions;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}