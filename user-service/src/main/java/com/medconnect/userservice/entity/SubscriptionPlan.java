package com.medconnect.userservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("subscription_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {
    @Id
    private String id;

    private String name;

    private PlanType planType;

    private Integer maxPatients;

    private Integer maxAppointmentsPerMonth;

    private Double monthlyPrice;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
