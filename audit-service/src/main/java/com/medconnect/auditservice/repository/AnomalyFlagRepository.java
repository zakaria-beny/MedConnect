package com.medconnect.auditservice.repository;

import com.medconnect.auditservice.model.AnomalyFlag;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AnomalyFlagRepository extends MongoRepository<AnomalyFlag, String> {
    List<AnomalyFlag> findByResolvedFalseOrderByDetectedAtDesc();
}
