package com.medconnect.userservice.service;

import com.medconnect.userservice.entity.PlanType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class SubscriptionPaymentService {

    private final RestClient restClient;

    @Value("${medconnect.subscription.payment.provider:none}")
    private String paymentProvider;

    @Value("${medconnect.subscription.payment.stripe.secret-key:}")
    private String stripeSecretKey;

    public SubscriptionPaymentService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.stripe.com")
                .build();
    }

    public void validateUpgradePayment(String userId, PlanType previousPlan, PlanType requestedPlan, String paymentReference) {
        if (previousPlan == null || requestedPlan == null || requestedPlan.ordinal() <= previousPlan.ordinal()) {
            return;
        }

        String provider = StringUtils.hasText(paymentProvider) ? paymentProvider.trim().toLowerCase() : "none";
        if ("none".equals(provider)) {
            return;
        }

        if (!StringUtils.hasText(paymentReference)) {
            throw new RuntimeException("Payment reference is required for plan upgrades.");
        }

        if ("stripe".equals(provider)) {
            verifyStripePaymentIntent(paymentReference);
            return;
        }

        throw new RuntimeException("Unsupported payment provider: " + paymentProvider);
    }

    private void verifyStripePaymentIntent(String paymentReference) {
        if (!StringUtils.hasText(stripeSecretKey)) {
            throw new RuntimeException("Stripe secret key is required when payment provider is stripe.");
        }

        try {
            Map<String, Object> response = restClient.get()
                    .uri("/v1/payment_intents/{paymentIntentId}", paymentReference)
                    .headers(headers -> headers.setBearerAuth(stripeSecretKey))
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new RuntimeException("Stripe payment verification returned an empty response.");
            }

            Object status = response.get("status");
            if (!"succeeded".equals(status)) {
                throw new RuntimeException("Stripe payment is not completed. Current status: " + status);
            }
        } catch (RestClientException ex) {
            throw new RuntimeException("Unable to verify Stripe payment.", ex);
        }
    }
}
