package com.medconnect.userservice.service;

import com.medconnect.userservice.entity.PlanType;
import com.medconnect.userservice.entity.Subscription;
import com.medconnect.userservice.entity.SubscriptionPlan;
import com.medconnect.userservice.entity.SubscriptionStatus;
import com.medconnect.userservice.repository.SubscriptionRepository;
import com.medconnect.userservice.repository.SubscriptionPlanRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionPlanPolicyService {

    public record PlanLimits(int maxPatients, int maxAppointmentsPerMonth) {
    }

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlanPolicyService(
            SubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository subscriptionPlanRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    @PostConstruct
    public void initializeDefaultPlans() {
        // Seed default plans if they don't exist
        if (subscriptionPlanRepository.findByPlanType(PlanType.BASIC).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            subscriptionPlanRepository.save(SubscriptionPlan.builder()
                    .name("Basic Plan")
                    .planType(PlanType.BASIC)
                    .maxPatients(200)
                    .maxAppointmentsPerMonth(500)
                    .monthlyPrice(0.0)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        }

        if (subscriptionPlanRepository.findByPlanType(PlanType.PREMIUM).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            subscriptionPlanRepository.save(SubscriptionPlan.builder()
                    .name("Premium Plan")
                    .planType(PlanType.PREMIUM)
                    .maxPatients(1_000)
                    .maxAppointmentsPerMonth(3_000)
                    .monthlyPrice(99.99)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        }

        if (subscriptionPlanRepository.findByPlanType(PlanType.ENTERPRISE).isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            subscriptionPlanRepository.save(SubscriptionPlan.builder()
                    .name("Enterprise Plan")
                    .planType(PlanType.ENTERPRISE)
                    .maxPatients(10_000)
                    .maxAppointmentsPerMonth(20_000)
                    .monthlyPrice(999.99)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        }
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

        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanType(planType).orElse(null);
        if (plan != null) {
            return new PlanLimits(plan.getMaxPatients(), plan.getMaxAppointmentsPerMonth());
        }

        // Fallback to defaults if plan not found in database
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
