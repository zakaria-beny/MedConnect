package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.ImagingResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ImagingResultRepository extends MongoRepository<ImagingResult, String> {
    List<ImagingResult> findByPatientIdOrderByStudyDateDesc(String patientId);
    List<ImagingResult> findByPatientIdAndStudyType(String patientId, String studyType);
    List<ImagingResult> findByPatientIdAndStudyDateBetween(String patientId, LocalDateTime startDate, LocalDateTime endDate);
}
