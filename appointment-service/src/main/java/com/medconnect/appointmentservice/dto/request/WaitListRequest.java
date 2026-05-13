package com.medconnect.appointmentservice.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO for adding a patient to the wait list.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitListRequest {
    @NotBlank(message = "Patient ID is required")
    private String patientId;
    
    @NotBlank(message = "Doctor ID is required")
    private String doctorId;
    
    @NotNull(message = "Requested date is required")
    @FutureOrPresent(message = "Requested date cannot be in the past")
    private LocalDate requestedDate;
}
