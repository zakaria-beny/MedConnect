package com.medconnect.appointmentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * MongoDB document for no-show records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "no_shows")
public class NoShow {
    @Id
    private String id;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private LocalDateTime scheduledTime;
    private LocalDateTime markedAt;
}
