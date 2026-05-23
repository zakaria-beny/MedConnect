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
public class SendToPharmacyRequest {
    @NotBlank(message = "Pharmacy ID is required")
    private String pharmacyId;
}
