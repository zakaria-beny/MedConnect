package com.mediconnect.notification.service;

import com.mediconnect.notification.document.DeliveryLog;
import com.mediconnect.notification.repository.DeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final DeliveryLogRepository deliveryLogRepository;

    // Envoyer une notification push via Firebase (simulation)
    public void sendPush(String deviceToken, String title, String message) {
        DeliveryLog log = new DeliveryLog();
        log.setUserId(deviceToken);
        log.setChannel("PUSH");
        log.setLastAttemptAt(LocalDateTime.now());
        log.setAttempts(1);

        try {
            // Dans un vrai projet → appel Firebase FCM :
            // FirebaseMessaging.getInstance().send(
            //     Message.builder()
            //         .setToken(deviceToken)
            //         .setNotification(Notification.builder()
            //             .setTitle(title)
            //             .setBody(message)
            //             .build())
            //         .build());
            System.out.println("🔔 Push envoyé à " + deviceToken + ": " + title);
            log.setStatus("SENT");
            log.setDeliveredAt(LocalDateTime.now());
        } catch (Exception e) {
            log.setStatus("FAILED");
            log.setErrorMessage(e.getMessage());
        }

        deliveryLogRepository.save(log);
    }

    public DeliveryLog trackDelivery(String pushId) {
        return deliveryLogRepository.findById(pushId)
                .orElseThrow(() -> new RuntimeException("Push non trouvé"));
    }

    public void handleTokenExpiry(String userId, String deviceId) {
        System.out.println("🔄 Token expiré pour user: " + userId
                + " device: " + deviceId);
    }
}