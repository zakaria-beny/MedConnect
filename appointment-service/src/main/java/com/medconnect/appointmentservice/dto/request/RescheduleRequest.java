package com.medconnect.appointmentservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for rescheduling an appointment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleRequest {
    @NotNull(message = "New date and time is required")
    @Future(message = "New appointment date must be in the future")
    private LocalDateTime newDateTime;
}
