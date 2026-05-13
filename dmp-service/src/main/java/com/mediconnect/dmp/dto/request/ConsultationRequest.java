package com.mediconnect.dmp.dto.request;

import com.mediconnect.dmp.model.Consultation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequest {

    @NotBlank(message = "Doctor ID is required")
    private String doctorId;

    @NotBlank(message = "Doctor name is required")
    private String doctorName;

    private String appointmentId;

    @NotNull(message = "Consultation date is required")
    private LocalDateTime consultationDate;

    @NotNull(message = "Consultation type is required")
    private Consultation.ConsultationType type;

    @NotBlank(message = "Chief complaint is required")
    private String chiefComplaint;

    private String clinicalFindings;

    private String assessment;

    private String plan;

    private List<String> diagnosesCodes;

    private String prescriptionId;

    private String followUpInstructions;

    private LocalDateTime followUpDate;
}
