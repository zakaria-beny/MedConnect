package com.medconnect.auditservice.repository;

import com.medconnect.auditservice.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByActorIdOrderByCreatedAtDesc(String actorId);
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
    List<AuditLog> findByDetailsContainingOrderByCreatedAtDesc(String details);
    List<AuditLog> findByResourceIdOrderByCreatedAtDesc(String resourceId);
    List<AuditLog> findByResourceTypeAndResourceIdOrderByCreatedAtDesc(String resourceType, String resourceId);
    
    @Query("{ 'createdAt': { $gte: ?0, $lt: ?1 } }")
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
