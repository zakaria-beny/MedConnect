package com.medconnect.appointmentservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for setting up or updating a doctor's schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {
    @NotEmpty(message = "Work days are required")
    private List<String> workDays;
    
    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Start time must be in HH:mm format (e.g. 08:00)")
    private String startTime;
    
    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "End time must be in HH:mm format (e.g. 17:00)")
    private String endTime;
    
    @NotBlank(message = "Lunch start time is required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Lunch start must be in HH:mm format (e.g. 12:00)")
    private String lunchStart;
    
    @NotBlank(message = "Lunch end time is required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Lunch end must be in HH:mm format (e.g. 13:00)")
    private String lunchEnd;
    
    @Min(value = 1, message = "Appointment duration must be at least 1 minute")
    @Max(value = 480, message = "Appointment duration cannot exceed 480 minutes (8 hours)")
    private int appointmentDurationMinutes;
}
