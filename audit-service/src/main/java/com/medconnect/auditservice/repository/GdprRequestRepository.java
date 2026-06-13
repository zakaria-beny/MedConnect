package com.medconnect.auditservice.repository;

import com.medconnect.auditservice.model.GdprRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface GdprRequestRepository extends MongoRepository<GdprRequest, String> {
    List<GdprRequest> findByUserIdOrderByCreatedAtDesc(String userId);
    
    @Query("{ 'createdAt': { $gte: ?0, $lt: ?1 } }")
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
