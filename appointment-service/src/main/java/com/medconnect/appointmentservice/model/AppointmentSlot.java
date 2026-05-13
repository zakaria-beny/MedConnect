package com.medconnect.appointmentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

/**
 * MongoDB document for appointment slots.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appointment_slots")
public class AppointmentSlot {
    @Id
    private String id;
    private String doctorId;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private boolean booked;
    private String appointmentId;
}
