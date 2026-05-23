package com.mediconnect.prescription.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class DispensationResponse {
    private String id;
    private String prescriptionId;
    private String patientId;
    private String pharmacyId;
    private String pharmacistId;
    private LocalDateTime dispensedAt;
    private List<DispensedItemResponse> items;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
