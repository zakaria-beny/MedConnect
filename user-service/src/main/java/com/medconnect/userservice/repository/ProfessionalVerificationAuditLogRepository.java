package com.medconnect.userservice.repository;

import com.medconnect.userservice.entity.ProfessionalVerificationAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProfessionalVerificationAuditLogRepository extends MongoRepository<ProfessionalVerificationAuditLog, String> {
    List<ProfessionalVerificationAuditLog> findByUserIdOrderByCreatedAtDesc(String userId);
}
