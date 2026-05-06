package com.medconnect.userservice.repository;

import com.medconnect.userservice.entity.PlanType;
import com.medconnect.userservice.entity.SubscriptionPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends MongoRepository<SubscriptionPlan, String> {
    Optional<SubscriptionPlan> findByPlanType(PlanType planType);
}
