package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.AccessLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessLogRepository extends MongoRepository<AccessLog, String> {

    List<AccessLog> findByPatientIdOrderByAccessedAtDesc(String patientId);

    List<AccessLog> findByPatientIdAndAuthorizedFalse(String patientId);

    void deleteByPatientId(String patientId);
}
