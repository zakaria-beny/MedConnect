package com.medconnect.userservice.dto;

import com.medconnect.userservice.entity.ProfessionalVerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfessionalVerificationUpdateRequest {
    @NotNull
    private ProfessionalVerificationStatus status;

    @Size(max = 500)
    private String note;
}
