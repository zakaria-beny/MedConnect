package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.LabResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabResultRepository extends MongoRepository<LabResult, String> {

    List<LabResult> findByPatientIdOrderByResultDateDesc(String patientId);

    List<LabResult> findByPatientIdAndCategory(String patientId, LabResult.LabCategory category);

    List<LabResult> findByOrderedByDoctorId(String doctorId);

    void deleteByPatientId(String patientId);
}