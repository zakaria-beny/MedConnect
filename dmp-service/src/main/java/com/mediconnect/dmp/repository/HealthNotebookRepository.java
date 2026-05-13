package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.HealthNotebookEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HealthNotebookRepository extends MongoRepository<HealthNotebookEntry, String> {

    List<HealthNotebookEntry> findByPatientIdOrderByMeasuredAtDesc(String patientId);

    // Find entries in a date range (used for trend graphs)
    List<HealthNotebookEntry> findByPatientIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            String patientId, LocalDateTime from, LocalDateTime to);

    // Find flagged (abnormal) entries
    List<HealthNotebookEntry> findByPatientIdAndFlaggedTrue(String patientId);

    void deleteByPatientId(String patientId);
}