package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.PatientDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<PatientDocument, String> {
    List<PatientDocument> findByPatientIdOrderByCreatedAtDesc(String patientId);
    List<PatientDocument> findByPatientIdAndDocumentType(String patientId, String documentType);
}