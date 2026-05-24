package com.medconnect.appointmentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for check-in response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private String appointmentId;
    private int queuePosition;
    private int estimatedWaitMinutes;
}
