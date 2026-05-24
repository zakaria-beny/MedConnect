package com.medconnect.appointmentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO for vacation period response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationResponse {
    private String id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
