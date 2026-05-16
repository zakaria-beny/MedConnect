package com.mediconnect.prescription.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrescriptionItemResponse {
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
