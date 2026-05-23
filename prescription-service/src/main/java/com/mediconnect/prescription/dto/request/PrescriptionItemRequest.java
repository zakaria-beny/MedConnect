package com.mediconnect.prescription.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionItemRequest {
    @NotBlank(message = "Drug name is required")
    private String drugName;

    private String activeIngredient;

    @NotBlank(message = "Dosage is required")
    private String dosage;

    @NotBlank(message = "Frequency is required")
    private String frequency;

    @NotBlank(message = "Route is required")
    private String route;

    private int durationDays;
    private String instructions;
    private int quantity;
    private boolean substitutionAllowed;
}
