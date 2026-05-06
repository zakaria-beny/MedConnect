package com.medconnect.userservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PatientProfileResponse {
    private String id;
    private String userId;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String insuranceNumber;
    private List<String> allergies;
}
