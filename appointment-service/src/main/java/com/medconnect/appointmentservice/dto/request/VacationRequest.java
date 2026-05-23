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
 * DTO for adding a vacation period.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationRequest {
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Vacation start date cannot be in the past")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "Vacation end date cannot be in the past")
    private LocalDate endDate;
    
    @NotBlank(message = "Reason is required")
    private String reason;
}
