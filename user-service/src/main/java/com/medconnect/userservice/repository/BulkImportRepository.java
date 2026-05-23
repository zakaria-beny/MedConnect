package com.medconnect.userservice.repository;

import com.medconnect.userservice.entity.BulkImport;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BulkImportRepository extends MongoRepository<BulkImport, String> {
    Optional<BulkImport> findByIdAndUserId(String id, String userId);
}
