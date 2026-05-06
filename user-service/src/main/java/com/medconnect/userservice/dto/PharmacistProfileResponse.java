package com.medconnect.userservice.dto;

import lombok.Data;

@Data
public class PharmacistProfileResponse {
    private String id;
    private String userId;
    private String finessNumber;
    private String pharmacyName;
    private String city;
    private String openingHours;
    private boolean deliveryAvailable;
}
