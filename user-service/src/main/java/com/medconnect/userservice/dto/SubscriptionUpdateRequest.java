package com.medconnect.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubscriptionUpdateRequest {
    @NotBlank
    private String planType;

    private String paymentReference;
}
