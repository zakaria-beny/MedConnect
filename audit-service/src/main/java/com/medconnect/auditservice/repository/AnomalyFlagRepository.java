package com.medconnect.auditservice.repository;

import com.medconnect.auditservice.model.AnomalyFlag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AnomalyFlagRepository extends MongoRepository<AnomalyFlag, String> {
    List<AnomalyFlag> findByResolvedFalseOrderByDetectedAtDesc();
    List<AnomalyFlag> findByUserIdOrderByDetectedAtDesc(String userId);
    
    long countBySeverityAndDetectedAtAfter(String severity, LocalDateTime cutoff);
    
    @Query("{ 'detectedAt': { $gte: ?0 } }")
    long countByResolvedTrueAndDetectedAtAfter(LocalDateTime cutoff);
    
    @Query("{ 'detectedAt': { $gte: ?0, $lt: ?1 } }")
    long countByDetectedAtBetween(LocalDateTime start, LocalDateTime end);
}
