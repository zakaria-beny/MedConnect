package com.medconnect.userservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PatientProfileRequest {
    private String userId;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String insuranceNumber;
    private List<String> allergies;
}
