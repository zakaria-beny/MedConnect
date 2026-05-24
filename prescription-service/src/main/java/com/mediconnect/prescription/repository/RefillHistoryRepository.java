package com.mediconnect.prescription.repository;

import com.mediconnect.prescription.model.RefillHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefillHistoryRepository extends MongoRepository<RefillHistory, String> {
    List<RefillHistory> findByOriginalPrescriptionId(String prescriptionId);

    List<RefillHistory> findByPatientId(String patientId);

    List<RefillHistory> findByStatus(RefillHistory.RefillStatus status);
}
