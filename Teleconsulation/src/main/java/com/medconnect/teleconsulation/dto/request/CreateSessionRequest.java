package com.medconnect.teleconsulation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSessionRequest {

    @NotBlank(message = "Appointment ID is required")
    private String appointmentId;

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    @NotBlank(message = "Patient ID is required")
    private String patientId;
}
