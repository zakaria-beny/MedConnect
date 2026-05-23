package com.mediconnect.notification.service;

import com.mediconnect.notification.repository.DeliveryLogRepository;
import com.mediconnect.notification.repository.NotificationRepository;
import com.mediconnect.notification.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final DeliveryLogRepository deliveryLogRepository;
    private final NotificationRepository notificationRepository;
    private final UserPreferencesRepository userPreferencesRepository;

    public Map<String, Object> trackDeliveryRate(String channel) {
        long total = deliveryLogRepository.findByStatus("SENT")
                .stream()
                .filter(log -> log.getChannel().equals(channel))
                .count();

        long failed = deliveryLogRepository.findByStatus("FAILED")
                .stream()
                .filter(log -> log.getChannel().equals(channel))
                .count();

        double rate = total > 0 ?
                (double) total / (total + failed) * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("channel", channel);
        stats.put("sent", total);
        stats.put("failed", failed);
        stats.put("deliveryRate", String.format("%.2f%%", rate));
        return stats;
    }

    public Map<String, Object> trackOptOutRate() {
        long totalUsers = userPreferencesRepository.count();
        long optedOut = userPreferencesRepository.findAll()
                .stream()
                .filter(prefs -> prefs.getOptedOutTypes() != null
                        && !prefs.getOptedOutTypes().isEmpty())
                .count();

        double rate = totalUsers > 0 ?
                (double) optedOut / totalUsers * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("optedOut", optedOut);
        stats.put("optOutRate", String.format("%.2f%%", rate));
        return stats;
    }

    public Map<String, Object> trackEngagementRate() {
        long total = notificationRepository.count();
        long read = notificationRepository.findAll()
                .stream()
                .filter(n -> n.isRead())
                .count();

        double rate = total > 0 ?
                (double) read / total * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("read", read);
        stats.put("engagementRate", String.format("%.2f%%", rate));
        return stats;
    }

    public Map<String, Object> getChannelEffectiveness() {
        Map<String, Object> effectiveness = new HashMap<>();
        effectiveness.put("SMS", trackDeliveryRate("SMS"));
        effectiveness.put("EMAIL", trackDeliveryRate("EMAIL"));
        effectiveness.put("PUSH", trackDeliveryRate("PUSH"));
        effectiveness.put("IN_APP", trackDeliveryRate("IN_APP"));
        return effectiveness;
    }
}