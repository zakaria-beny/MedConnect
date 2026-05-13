package com.mediconnect.dmp.dto.response;

import com.mediconnect.dmp.model.Consultation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationResponse {
    private String id;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private String appointmentId;
    private LocalDateTime consultationDate;
    private Consultation.ConsultationType type;
    private String chiefComplaint;
    private String clinicalFindings;
    private String assessment;
    private String plan;
    private List<String> diagnosesCodes;
    private String prescriptionId;
    private String followUpInstructions;
    private LocalDateTime followUpDate;
    private LocalDateTime createdAt;
}