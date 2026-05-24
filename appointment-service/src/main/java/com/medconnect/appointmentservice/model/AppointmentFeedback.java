package com.medconnect.appointmentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * MongoDB document for appointment feedback.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "appointment_feedback")
public class AppointmentFeedback {
    @Id
    private String id;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private int rating;
    private String comments;
    private LocalDateTime submittedAt;
}
