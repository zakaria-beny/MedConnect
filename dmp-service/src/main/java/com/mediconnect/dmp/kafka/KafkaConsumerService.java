package com.mediconnect.dmp.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    @KafkaListener(topics = "user.created", groupId = "dmp-service-group")
    public void handleUserCreated(Map<String, Object> event) {
        try {
            String userId = (String) event.get("userId");
            String role = (String) event.get("role");
            
            log.info("Received user.created event for user: {}, role: {}", userId, role);
            
            if ("PATIENT".equals(role)) {
                log.info("PATIENT user created - DMP will be initialized for userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("Error processing user.created event", e);
        }
    }

    @KafkaListener(topics = "prescription.dispensed", groupId = "dmp-service-group")
    public void handlePrescriptionDispensed(Map<String, Object> event) {
        try {
            String patientId = (String) event.get("patientId");
            String prescriptionId = (String) event.get("prescriptionId");
            String drugName = (String) event.get("drugName");
            
            log.info("Received prescription.dispensed event - patientId: {}, prescriptionId: {}, drugName: {}", 
                    patientId, prescriptionId, drugName);
        } catch (Exception e) {
            log.error("Error processing prescription.dispensed event", e);
        }
    }
}
