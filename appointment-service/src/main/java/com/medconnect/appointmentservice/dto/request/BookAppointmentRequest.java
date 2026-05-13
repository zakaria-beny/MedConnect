package com.medconnect.appointmentservice.dto.request;

import com.medconnect.appointmentservice.model.AppointmentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for booking an appointment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentRequest {
    @NotBlank(message = "Patient ID is required")
    private String patientId;
    
    @NotBlank(message = "Doctor ID is required")
    private String doctorId;
    
    @NotNull(message = "Date and time is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime dateTime;
    
    @NotNull(message = "Appointment type is required")
    private AppointmentType type;
    
    @NotBlank(message = "Reason is required")
    private String reason;
}
