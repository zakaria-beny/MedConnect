package com.mediconnect.dmp.dto.request;

import com.mediconnect.dmp.model.ChronicCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChronicConditionRequest {

    @NotBlank(message = "Condition name is required")
    private String conditionName;

    @NotBlank(message = "ICD-10 code is required")
    private String icd10Code;

    private LocalDate diagnosedDate;

    @NotBlank(message = "Diagnosed by is required")
    private String diagnosedBy;

    @NotNull(message = "Status is required")
    private ChronicCondition.ConditionStatus status;

    private String notes;

    private String treatmentPlan;
}
