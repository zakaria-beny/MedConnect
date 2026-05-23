package com.mediconnect.prescription.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispenseRequest {
    @NotBlank(message = "Pharmacy ID is required")
    private String pharmacyId;

    @NotBlank(message = "Pharmacist ID is required")
    private String pharmacistId;

    @Valid
    private List<DispensedItemRequest> items;

    private String notes;
}
