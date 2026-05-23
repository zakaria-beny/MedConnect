package com.medconnect.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class DoctorProfileRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String professionalRegistrationNumber;
    @NotBlank
    private String nationalIdNumber;
    private String registrationAuthority;
    @NotBlank
    private String specialty;
    private List<String> languages;
    private String city;
    private String clinicName;
    private String cardFrontImageUrl;
    private String cardBackImageUrl;
}
