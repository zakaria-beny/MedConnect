package com.medconnect.auditservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AuditEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${medconnect.kafka.topics.audit-report:audit.report.generated}")
    private String auditReportTopic;

    @Value("${medconnect.kafka.topics.anomaly-detected:anomaly.detected}")
    private String anomalyDetectedTopic;

    @Value("${medconnect.kafka.topics.gdpr-export-ready:gdpr.export.ready}")
    private String gdprExportReadyTopic;

    @Value("${medconnect.kafka.topics.gdpr-deletion-completed:gdpr.deletion.completed}")
    private String gdprDeletionCompletedTopic;

    public AuditEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAuditReportGenerated(String reportId, String month, int recordCount) {
        publish(auditReportTopic, "audit.report.generated", reportId, Map.of(
                "month", month,
                "recordCount", recordCount
        ));
    }

    public void publishAnomalyDetected(String anomalyId, String userId, String anomalyType, String description) {
        publish(anomalyDetectedTopic, "anomaly.detected", anomalyId, Map.of(
                "userId", userId,
                "anomalyType", anomalyType,
                "description", description
        ));
    }

    public void publishGdprExportReady(String exportId, String userId, String downloadUrl) {
        publish(gdprExportReadyTopic, "gdpr.export.ready", exportId, Map.of(
                "userId", userId,
                "downloadUrl", downloadUrl
        ));
    }

    public void publishGdprDeletionCompleted(String deletionId, String userId) {
        publish(gdprDeletionCompletedTopic, "gdpr.deletion.completed", deletionId, Map.of(
                "userId", userId
        ));
    }

    private void publish(String topic, String eventType, String key, Map<String, Object> details) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventType", eventType);
        event.put("timestamp", Instant.now().toString());
        event.put("details", details);

        try {
            kafkaTemplate.send(topic, key, event);
            log.info("Published Kafka event [{}] to topic [{}]", eventType, topic);
        } catch (Exception e) {
            log.warn("Failed to publish Kafka event [{}]: {}", eventType, e.getMessage());
        }
    }
}
