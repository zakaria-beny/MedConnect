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
import java.util.List;

@Document(collection = "dispensations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dispensation {

    @Id
    private String id;

    private String prescriptionId;
    private String patientId;
    private String pharmacyId;
    private String pharmacistId;
    private LocalDateTime dispensedAt;
    private List<DispensedItem> items;
    private DispensationStatus status;
    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    public enum DispensationStatus {
        COMPLETE, PARTIAL
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DispensedItem {
        private String drugName;
        private int quantityDispensed;
        private int quantityRemaining;
        private String batchNumber;
    }
}
