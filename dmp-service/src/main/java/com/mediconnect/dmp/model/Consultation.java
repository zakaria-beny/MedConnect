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
@Document(collection = "consultations")
public class Consultation {

    @Id
    private String id;

    private String patientId;
    private String doctorId;
    private String doctorName;

    // Reference to appointment (if booked through the system)
    private String appointmentId;

    // When the consultation took place
    private LocalDateTime consultationDate;

    // Type: in-person or teleconsultation
    private ConsultationType type;

    // What the patient complained about
    private String chiefComplaint;

    // Doctor's clinical findings from examination
    private String clinicalFindings;

    // Doctor's assessment (what they think is wrong)
    private String assessment;

    // What the doctor decided to do
    private String plan;

    // ICD-10 codes for diagnoses made during this consultation
    private List<String> diagnosesCodes;

    // Prescription created during this consultation
    private String prescriptionId;

    // Follow-up instructions
    private String followUpInstructions;

    // Date of next appointment
    private LocalDateTime followUpDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ConsultationType {
        IN_PERSON,
        TELECONSULTATION,
        HOME_VISIT,
        EMERGENCY
    }
}