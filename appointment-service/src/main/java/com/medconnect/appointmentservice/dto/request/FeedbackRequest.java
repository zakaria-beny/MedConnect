package com.medconnect.appointmentservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for submitting feedback on an appointment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;
    
    private String comments;
}
