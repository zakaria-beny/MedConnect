package com.mediconnect.dmp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationRequest {

    @NotBlank(message = "Vaccine name is required")
    private String vaccineName;

    @NotBlank(message = "Target disease is required")
    private String targetDisease;

    @NotNull(message = "Administration date is required")
    private LocalDate administrationDate;

    private String lotNumber;

    private String manufacturer;

    private int doseNumber;

    private Integer totalDosesRequired;

    private LocalDate nextDoseDate;

    private String administeredAt;

    private String administeredBy;

    private String adverseReactions;
}
