package com.medconnect.appointmentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * MongoDB document for check-ins.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "check_ins")
public class CheckIn {
    @Id
    private String id;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private LocalDateTime checkInTime;
    private int queuePosition;
    private int estimatedWaitMinutes;
}
