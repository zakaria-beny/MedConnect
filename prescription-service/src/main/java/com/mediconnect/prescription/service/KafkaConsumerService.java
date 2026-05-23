package com.mediconnect.prescription.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(topics = "dmp.updated", groupId = "prescription-service-group")
    public void handleDmpUpdated(Map<String, Object> event) {
        try {
            String patientId = (String) event.get("patientId");
            log.info("DMP updated for patient: {}", patientId);
        } catch (Exception e) {
            log.error("Error handling dmp.updated event", e);
        }
    }
}
