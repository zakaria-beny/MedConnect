package com.medconnect.userservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class DoctorProfileResponse {
    private String id;
    private String userId;
    private String rppsLicense;
    private String specialty;
    private List<String> languages;
    private String city;
    private String clinicName;
}
