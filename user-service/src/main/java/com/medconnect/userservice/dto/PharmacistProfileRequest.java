package com.medconnect.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PharmacistProfileRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String finessNumber;
    @NotBlank
    private String pharmacyName;
    private String city;
    private String openingHours;
    private boolean deliveryAvailable;
}
