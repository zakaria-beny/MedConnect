package com.medconnect.auditservice.kafka;

import com.medconnect.auditservice.dto.AuditLogRequest;
import com.medconnect.auditservice.service.AuditService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

    private final AuditService auditService;

    public AuditEventListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(topics = {
            "user.login",
            "user.logout",
            "user.created",
            "dmp.updated",
            "dmp.accessed",
            "prescription.created",
            "prescription.signed",
            "prescription.dispensed",
            "appointment.booked",
            "teleconsult.started",
            "message.sent"
    }, groupId = "audit-service-group")
    public void handleEvent(ConsumerRecord<String, String> record) {
        AuditLogRequest request = new AuditLogRequest();
        request.setActorId("SYSTEM");
        request.setAction(record.topic());
        request.setResourceType("KAFKA_EVENT");
        request.setResourceId(record.topic());
        request.setDetails(record.value());
        auditService.logAction(request);
    }
}
