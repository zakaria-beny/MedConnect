package com.medconnect.appointmentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Embedded document for vacation periods.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationPeriod {
    private String id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
