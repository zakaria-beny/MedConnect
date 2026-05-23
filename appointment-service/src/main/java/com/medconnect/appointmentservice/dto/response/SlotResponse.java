package com.medconnect.appointmentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * DTO for appointment slot response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotResponse {
    private String id;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private boolean isAvailable;
}
