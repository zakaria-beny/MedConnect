package com.medconnect.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class DoctorProfileRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String rppsLicense;
    @NotBlank
    private String specialty;
    private List<String> languages;
    private String city;
    private String clinicName;
}
