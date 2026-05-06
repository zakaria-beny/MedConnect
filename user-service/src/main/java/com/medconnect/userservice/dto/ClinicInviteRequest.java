package com.medconnect.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClinicInviteRequest {
    @Email
    @NotBlank
    private String userEmail;
}
