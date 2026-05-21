package com.medconnect.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PharmacistProfileRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String professionalRegistrationNumber;
    @NotBlank
    private String nationalIdNumber;
    private String registrationAuthority;
    @NotBlank
    private String pharmacyName;
    private String city;
    private String openingHours;
    private boolean deliveryAvailable;
    private String cardFrontImageUrl;
    private String cardBackImageUrl;
}
