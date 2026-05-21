package com.mediconnect.notification.service;

import com.mediconnect.notification.repository.DeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final DeliveryLogRepository deliveryLogRepository;

    private static final int MAX_SMS_PER_DAY = 5;

    public boolean enforceRateLimit(String userId, String channel) {
        if (channel.equals("SMS")) {
            long smsSentToday = deliveryLogRepository
                    .findByStatus("SENT")
                    .stream()
                    .filter(log -> log.getUserId().equals(userId)
                            && log.getChannel().equals("SMS")
                            && log.getDeliveredAt() != null
                            && log.getDeliveredAt().isAfter(
                            LocalDateTime.now().minusDays(1)))
                    .count();
            return smsSentToday < MAX_SMS_PER_DAY;
        }
        return true;
    }

    public LocalDateTime calculateNextAllowed(String userId) {
        return LocalDateTime.now().plusHours(1);
    }

    public boolean prioritizeUrgent(boolean isUrgent) {
        // Les notifications urgentes bypass la limite
        return isUrgent;
    }
}