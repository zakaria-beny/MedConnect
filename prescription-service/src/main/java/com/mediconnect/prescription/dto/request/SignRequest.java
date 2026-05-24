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
public class SignRequest {
    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    private String certificate;
    private String signatureImage;
}
