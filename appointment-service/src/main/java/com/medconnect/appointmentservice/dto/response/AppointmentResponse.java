package com.medconnect.appointmentservice.dto.response;

import com.medconnect.appointmentservice.model.AppointmentStatus;
import com.medconnect.appointmentservice.model.AppointmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for appointment response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private String id;
    private String patientId;
    private String doctorId;
    private String dateTime;
    private String formattedDateTime;
    private AppointmentType type;
    private AppointmentStatus status;
    private String reason;
    private String cancellationReason;
    private String createdAt;
    private String updatedAt;
}
