package com.mediconnect.prescription.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mediconnect.prescription.model.Prescription;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrescriptionResponse {
    private String id;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private List<PrescriptionItemResponse> items;
    private String status;
    private String digitalSignature;
    private LocalDateTime signedAt;
    private String qrCodeBase64;
    private String pharmacyId;
    private LocalDateTime sentToPharmacyAt;
    private LocalDateTime dispensedAt;
    private String dispensedByPharmacistId;
    private int refillsAllowed;
    private int refillsUsed;
    private boolean controlledSubstance;
    private String notes;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
