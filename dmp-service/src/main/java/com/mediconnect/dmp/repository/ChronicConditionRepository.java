package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.ChronicCondition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChronicConditionRepository extends MongoRepository<ChronicCondition, String> {

    List<ChronicCondition> findByPatientId(String patientId);

    List<ChronicCondition> findByPatientIdAndStatus(String patientId, ChronicCondition.ConditionStatus status);

    Optional<ChronicCondition> findByPatientIdAndIcd10Code(String patientId, String icd10Code);

    void deleteByPatientId(String patientId);
}