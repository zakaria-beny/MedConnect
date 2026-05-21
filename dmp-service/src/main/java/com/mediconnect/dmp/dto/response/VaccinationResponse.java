package com.mediconnect.dmp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationResponse {
    private String id;
    private String patientId;
    private String vaccineName;
    private String targetDisease;
    private LocalDate administrationDate;
    private String lotNumber;
    private String manufacturer;
    private int doseNumber;
    private Integer totalDosesRequired;
    private LocalDate nextDoseDate;
    private String administeredAt;
    private String administeredBy;
    private String adverseReactions;
    private LocalDateTime createdAt;
}