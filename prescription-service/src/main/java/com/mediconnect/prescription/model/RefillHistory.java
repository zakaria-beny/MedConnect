package com.mediconnect.prescription.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "refill_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefillHistory {

    @Id
    private String id;

    private String originalPrescriptionId;
    private String patientId;
    private String requestedByPatientId;
    private String approvedByDoctorId;
    private RefillStatus status;
    private String reason;

    @CreatedDate
    private LocalDateTime requestedAt;

    private LocalDateTime processedAt;

    public enum RefillStatus {
        PENDING, APPROVED, REJECTED
    }
}
