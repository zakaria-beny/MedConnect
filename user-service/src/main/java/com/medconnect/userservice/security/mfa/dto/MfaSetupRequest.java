package com.medconnect.userservice.security.mfa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaSetupRequest {
    @NotBlank
    private String method;
    private String phoneNumber;
}
