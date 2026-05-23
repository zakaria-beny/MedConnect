package com.mediconnect.notification.service;

import com.mediconnect.notification.document.DeliveryLog;
import com.mediconnect.notification.repository.DeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final DeliveryLogRepository deliveryLogRepository;

    public void sendEmail(String to, String subject, String body) {
        DeliveryLog log = new DeliveryLog();
        log.setUserId(to);
        log.setChannel("EMAIL");
        log.setLastAttemptAt(LocalDateTime.now());
        log.setAttempts(1);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.setStatus("SENT");
            log.setDeliveredAt(LocalDateTime.now());
        } catch (Exception e) {
            log.setStatus("FAILED");
            log.setErrorMessage(e.getMessage());
        }

        deliveryLogRepository.save(log);
    }

    public void handleBounce(String emailId) {
        deliveryLogRepository.findById(emailId).ifPresent(log -> {
            log.setStatus("BOUNCED");
            deliveryLogRepository.save(log);
        });
    }

    public void retryOnFailure(String emailId) {
        deliveryLogRepository.findById(emailId).ifPresent(log -> {
            if (log.getAttempts() < 3) {
                log.setAttempts(log.getAttempts() + 1);
                log.setLastAttemptAt(LocalDateTime.now());
                deliveryLogRepository.save(log);
            }
        });
    }
}