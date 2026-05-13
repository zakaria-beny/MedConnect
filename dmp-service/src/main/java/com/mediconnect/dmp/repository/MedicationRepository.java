package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.Medication;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends MongoRepository<Medication, String> {

    List<Medication> findByPatientId(String patientId);

    // Get only currently active medications
    List<Medication> findByPatientIdAndStatus(String patientId, Medication.MedicationStatus status);

    // Find medications that need refills (refillsRemaining > 0)
    List<Medication> findByPatientIdAndRefillsRemainingGreaterThan(String patientId, int count);

    // Find by prescription reference
    List<Medication> findByPrescriptionId(String prescriptionId);

    void deleteByPatientId(String patientId);
}