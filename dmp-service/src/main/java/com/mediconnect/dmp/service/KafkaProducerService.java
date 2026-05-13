package com.mediconnect.dmp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String DMP_UPDATED_TOPIC = "dmp.updated";
    private static final String DMP_ACCESSED_TOPIC = "dmp.accessed";
    private static final String ALLERGY_ALERT_TOPIC = "allergy.alert";

    public void publishDmpUpdated(String patientId, String section, String action) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("patientId", patientId);
            event.put("section", section);
            event.put("action", action);
            event.put("timestamp", LocalDateTime.now());
            event.put("service", "dmp-service");

            kafkaTemplate.send(DMP_UPDATED_TOPIC, patientId, event);
            log.info("Published dmp.updated event for patient {} - section: {}, action: {}", patientId, section, action);
        } catch (Exception e) {
            log.error("Error publishing dmp.updated event for patient {}", patientId, e);
        }
    }

    public void publishDmpAccessed(String patientId, String accessedByUserId, java.util.List<String> sections) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("patientId", patientId);
            event.put("accessedByUserId", accessedByUserId);
            event.put("sections", sections);
            event.put("timestamp", LocalDateTime.now());
            event.put("service", "dmp-service");

            kafkaTemplate.send(DMP_ACCESSED_TOPIC, patientId, event);
            log.info("Published dmp.accessed event for patient {} accessed by user {}", patientId, accessedByUserId);
        } catch (Exception e) {
            log.error("Error publishing dmp.accessed event for patient {}", patientId, e);
        }
    }

    public void publishAllergyAlert(String patientId, String allergen, String severity) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("patientId", patientId);
            event.put("allergen", allergen);
            event.put("severity", severity);
            event.put("timestamp", LocalDateTime.now());
            event.put("service", "dmp-service");

            kafkaTemplate.send(ALLERGY_ALERT_TOPIC, patientId, event);
            log.info("Published allergy.alert event for patient {} - allergen: {}, severity: {}", patientId, allergen, severity);
        } catch (Exception e) {
            log.error("Error publishing allergy.alert event for patient {}", patientId, e);
        }
    }
}
