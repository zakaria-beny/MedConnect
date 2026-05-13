package com.medconnect.appointmentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

/**
 * MongoDB document for doctor schedules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "doctor_schedules")
public class DoctorSchedule {
    @Id
    private String id;
    private String doctorId;
    private List<String> workDays;
    private String startTime;
    private String endTime;
    private String lunchStart;
    private String lunchEnd;
    private int appointmentDurationMinutes;
    private List<VacationPeriod> vacationPeriods;
}
