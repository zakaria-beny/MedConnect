package com.mediconnect.prescription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String TOPIC_PRESCRIPTION_CREATED = "prescription.created";
    public static final String TOPIC_PRESCRIPTION_SIGNED = "prescription.signed";
    public static final String TOPIC_PRESCRIPTION_SENT = "prescription.sent";
    public static final String TOPIC_PRESCRIPTION_DISPENSED = "prescription.dispensed";
    public static final String TOPIC_PRESCRIPTION_EXPIRED = "prescription.expired";
    public static final String TOPIC_PRESCRIPTION_REFILLED = "prescription.refilled";

    public void publishPrescriptionCreated(String prescriptionId, String patientId, String doctorId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("prescriptionId", prescriptionId);
            event.put("patientId", patientId);
            event.put("doctorId", doctorId);
            event.put("timestamp", LocalDateTime.now());
            event.put("service", "prescription-service");

            kafkaTemplate.send(TOPIC_PRESCRIPTION_CREATED, prescriptionId, event);
            log.info("Published prescription.created event for prescription {}", prescriptionId);
        } catch (Exception e) {
            log.error("Error publishing prescription.created event", e);
        }
    }

    public void publishPrescriptionSigned(String prescriptionId, String doctorId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("prescriptionId", prescriptionId);
            event.put("doctorId", doctorId);
            event.put("timestamp", LocalDateTime.now());
            event.put("service", "prescription-service");

            kafkaTemplate.send(TOPIC_PRESCRIPTION_SIGNED, prescriptionId, event);
            log.info("Published prescription.signed event for prescription {}", prescriptionId);
        } catch (Exception e) {
            log.error("Error publishing prescription.signed event", e);
        }
    }

    public void publishPrescriptionSent(String prescriptionId, String pharmacyId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("prescriptionId", prescriptionId);
            event.put("pharmacyId", pharmacyId);
            event.put("timestamp", LocalDateTime.now());
            event.put("service", "prescription-service");

            kafkaTemplate.send(TOPIC_PRESCRIPTION_SENT, prescriptionId, event);
            log.info("Published prescription.sent event for prescription {}", prescriptionId);
        } catch (Exception e) {
            log.error("Error publishing prescription.sent event", e);
        }
    }

    public void publishPrescriptionDispensed(String prescriptionId, String patientId, String pharmacyId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("prescriptionId", prescriptionId);
            event.put("patientId", patientId);
            event.put("pharmacyId", pharmacyId);
            event.put("timestamp", LocalDateTime.now());
            event.put("service", "prescription-service");

            kafkaTemplate.send(TOPIC_PRESCRIPTION_DISPENSED, prescriptionId, event);
            log.info("Published prescription.dispensed event for prescription {}", prescriptionId);
        } catch (Exception e) {
            log.error("Error publishing prescription.dispensed event", e);
        }
    }

    public void publishPrescriptionExpired(String prescriptionId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("prescriptionId", prescriptionId);
            event.put("timestamp", LocalDateTime.now());
            event.put("service", "prescription-service");

            kafkaTemplate.send(TOPIC_PRESCRIPTION_EXPIRED, prescriptionId, event);
            log.info("Published prescription.expired event for prescription {}", prescriptionId);
        } catch (Exception e) {
            log.error("Error publishing prescription.expired event", e);
        }
    }

    public void publishPrescriptionRefilled(String prescriptionId, String patientId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("prescriptionId", prescriptionId);
            event.put("patientId", patientId);
            event.put("timestamp", LocalDateTime.now());
            event.put("service", "prescription-service");

            kafkaTemplate.send(TOPIC_PRESCRIPTION_REFILLED, prescriptionId, event);
            log.info("Published prescription.refilled event for prescription {}", prescriptionId);
        } catch (Exception e) {
            log.error("Error publishing prescription.refilled event", e);
        }
    }
}
