package com.medconnect.userservice.security.mfa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MfaVerifyRequest {
    @NotBlank
    private String method;
    @NotBlank
    private String code;
}
