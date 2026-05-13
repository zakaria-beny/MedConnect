package com.mediconnect.dmp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "lab_results")
public class LabResult {

    @Id
    private String id;

    private String patientId;

    // Who ordered this test
    private String orderedByDoctorId;

    // Name of the laboratory
    private String laboratoryName;

    // Type of test (e.g., "Complete Blood Count", "HbA1c")
    private String testName;

    // Category for easy filtering
    private LabCategory category;

    // When blood/sample was taken
    private LocalDateTime sampleCollectedAt;

    // When results were ready
    private LocalDateTime resultDate;

    // Individual test values (a CBC has many values inside)
    private List<LabValue> values;

    // Doctor's interpretation of the results
    private String interpretation;

    // Path to the PDF report file stored in the system
    private String reportFilePath;

    // Overall result status
    private ResultStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /*
     * Inner class (nested class inside LabResult).
     * We use it because LabValue only makes sense IN the context of a LabResult.
     * MongoDB will store this as an embedded array inside the lab_results document.
     *
     * Example: CBC result contains:
     *   - { parameterName: "Hemoglobin", value: "14.5", unit: "g/dL", referenceRange: "13.5-17.5", abnormal: false }
     *   - { parameterName: "WBC", value: "11.2", unit: "10^3/µL", referenceRange: "4.5-11.0", abnormal: true }
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabValue {
        private String parameterName;
        private String value;
        private String unit;
        private String referenceRange;
        private boolean abnormal;  // true = outside normal range = flag for doctor
    }

    public enum LabCategory {
        HEMATOLOGY,      // blood tests
        BIOCHEMISTRY,    // metabolic tests
        MICROBIOLOGY,    // bacteria/infection tests
        IMMUNOLOGY,      // immune system
        HORMONES,        // hormone levels
        URINE_ANALYSIS,  // urine tests
        GENETICS,        // DNA tests
        OTHER
    }

    public enum ResultStatus {
        PENDING,    // test ordered, results not yet available
        PARTIAL,    // some results available
        FINAL,      // all results available
        CORRECTED   // results were corrected after final release
    }
}