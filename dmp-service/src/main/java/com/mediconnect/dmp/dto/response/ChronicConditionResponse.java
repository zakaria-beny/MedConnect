package com.mediconnect.dmp.dto.response;

import com.mediconnect.dmp.model.ChronicCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChronicConditionResponse {
    private String id;
    private String patientId;
    private String conditionName;
    private String icd10Code;
    private LocalDate diagnosedDate;
    private String diagnosedBy;
    private ChronicCondition.ConditionStatus status;
    private String notes;
    private String treatmentPlan;
    private LocalDate lastReviewedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}