package com.medconnect.auditservice.repository;

import com.medconnect.auditservice.model.GdprRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GdprRequestRepository extends MongoRepository<GdprRequest, String> {
    List<GdprRequest> findByUserIdOrderByCreatedAtDesc(String userId);
}
