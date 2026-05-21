package com.mediconnect.dmp.dto.response;

import com.mediconnect.dmp.model.Allergy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllergyResponse {
    private String id;
    private String patientId;
    private String allergen;
    private Allergy.SeverityLevel severity;
    private String reaction;
    private String notes;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
