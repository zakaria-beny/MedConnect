package com.medconnect.appointmentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for doctor schedule response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private String id;
    private String doctorId;
    private List<String> workDays;
    private String startTime;
    private String endTime;
    private String lunchStart;
    private String lunchEnd;
    private int appointmentDurationMinutes;
    private List<VacationResponse> vacationPeriods;
}
