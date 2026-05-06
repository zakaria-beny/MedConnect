package com.medconnect.userservice.service;

import com.medconnect.userservice.entity.PlanType;
import com.medconnect.userservice.entity.Subscription;
import com.medconnect.userservice.entity.SubscriptionStatus;
import com.medconnect.userservice.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionPlanPolicyService {

    public record PlanLimits(int maxPatients, int maxAppointmentsPerMonth) {
    }

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionPlanPolicyService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public PlanType resolveCurrentPlan(String userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId).orElse(null);
        if (subscription == null || subscription.getPlanType() == null) {
            return PlanType.BASIC;
        }
        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            return null;
        }
        return subscription.getPlanType();
    }

    public PlanLimits getLimits(PlanType planType) {
        if (planType == null) {
            return new PlanLimits(0, 0);
        }
        return switch (planType) {
            case BASIC -> new PlanLimits(200, 500);
            case PREMIUM -> new PlanLimits(1_000, 3_000);
            case ENTERPRISE -> new PlanLimits(10_000, 20_000);
        };
    }

    public PlanLimits getLimitsForUser(String userId) {
        return getLimits(resolveCurrentPlan(userId));
    }
}
