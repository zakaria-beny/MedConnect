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

/*
 * The Health Notebook is where PATIENTS log their own vitals at home.
 * Example: A diabetic patient logs their blood sugar every morning.
 * This data is then shown to the doctor as a trend graph.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "health_notebook_entries")
public class HealthNotebookEntry {

    @Id
    private String id;

    private String patientId;

    // When the patient measured their vitals
    private LocalDateTime measuredAt;

    // Blood pressure: systolic (the top number, e.g., 120)
    private Integer bloodPressureSystolic;

    // Blood pressure: diastolic (the bottom number, e.g., 80)
    private Integer bloodPressureDiastolic;

    // Heart rate (beats per minute)
    private Integer heartRate;

    // Blood glucose level (mg/dL or mmol/L)
    private Double bloodGlucose;

    // Unit for blood glucose
    private String bloodGlucoseUnit;

    // Patient's weight (in kg)
    private Double weight;

    // Patient's height in cm (rarely changes, but useful for BMI)
    private Double height;

    // Body temperature (Celsius)
    private Double temperature;

    // Oxygen saturation percentage (e.g., 98%)
    private Integer oxygenSaturation;

    // How many steps walked today
    private Integer steps;

    // Patient's notes (e.g., "felt dizzy after measurement")
    private String patientNotes;

    // Was this measurement flagged as abnormal?
    @Builder.Default
    private boolean flagged = false;

    // System-generated alert message if abnormal (e.g., "Blood pressure too high")
    private String alertMessage;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}