package com.medconnect.userservice.dto;

import lombok.Data;

@Data
public class SubscriptionResponse {
    private String id;
    private String userId;
    private String planType;
    private String status;
    private Integer maxPatients;
    private Integer maxAppointmentsPerMonth;
}
