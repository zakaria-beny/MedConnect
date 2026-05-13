package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.Consent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentRepository extends MongoRepository<Consent, String> {

    // All active consents for a patient (who can see their records?)
    List<Consent> findByPatientIdAndActiveTrue(String patientId);

    // Check if a specific doctor has active consent for a patient
    Optional<Consent> findByPatientIdAndDoctorIdAndActiveTrue(String patientId, String doctorId);

    // All patients a doctor has consent to access
    List<Consent> findByDoctorIdAndActiveTrue(String doctorId);

    // Find expired consents (for cleanup jobs)
    List<Consent> findByActiveTrueAndExpiresAtBefore(LocalDateTime now);
}