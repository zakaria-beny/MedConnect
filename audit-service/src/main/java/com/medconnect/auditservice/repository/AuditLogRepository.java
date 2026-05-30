package com.medconnect.auditservice.repository;

import com.medconnect.auditservice.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByActorIdOrderByCreatedAtDesc(String actorId);
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
}
