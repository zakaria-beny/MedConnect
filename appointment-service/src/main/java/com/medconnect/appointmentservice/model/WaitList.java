package com.medconnect.appointmentservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MongoDB document for wait list.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wait_list")
public class WaitList {
    @Id
    private String id;
    private String patientId;
    private String doctorId;
    private LocalDate requestedDate;
    private LocalDateTime addedAt;
    private boolean notified;
}
