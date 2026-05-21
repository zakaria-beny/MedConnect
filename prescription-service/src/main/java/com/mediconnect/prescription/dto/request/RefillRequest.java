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
public class RefillRequest {
    @NotBlank(message = "Patient ID is required")
    private String patientId;

    private String reason;
}
