package com.mediconnect.notification.service;

import com.mediconnect.notification.document.DeliveryLog;
import com.mediconnect.notification.repository.DeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SMSService {

    private final DeliveryLogRepository deliveryLogRepository;

    // Envoyer un SMS via Twilio (simulation)
    public void sendSMS(String phoneNumber, String message) {
        DeliveryLog log = new DeliveryLog();
        log.setUserId(phoneNumber);
        log.setChannel("SMS");
        log.setLastAttemptAt(LocalDateTime.now());
        log.setAttempts(1);

        try {
            // Dans un vrai projet → appel API Twilio :
            // Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            // Message.creator(new PhoneNumber(phoneNumber),
            //     new PhoneNumber(FROM_NUMBER), message).create();
            System.out.println("📱 SMS envoyé à " + phoneNumber + ": " + message);
            log.setStatus("SENT");
            log.setDeliveredAt(LocalDateTime.now());
        } catch (Exception e) {
            log.setStatus("FAILED");
            log.setErrorMessage(e.getMessage());
        }

        deliveryLogRepository.save(log);
    }

    public DeliveryLog trackDelivery(String smsId) {
        return deliveryLogRepository.findById(smsId)
                .orElseThrow(() -> new RuntimeException("SMS non trouvé"));
    }

    public void retryOnFailure(String smsId) {
        deliveryLogRepository.findById(smsId).ifPresent(log -> {
            if (log.getAttempts() < 3) {
                log.setAttempts(log.getAttempts() + 1);
                log.setLastAttemptAt(LocalDateTime.now());
                deliveryLogRepository.save(log);
                sendSMS(log.getUserId(), "Retry message");
            }
        });
    }

    public void logSMSEvent(String smsId, String status, String error) {
        deliveryLogRepository.findById(smsId).ifPresent(log -> {
            log.setStatus(status);
            log.setErrorMessage(error);
            log.setLastAttemptAt(LocalDateTime.now());
            deliveryLogRepository.save(log);
        });
    }
}