package com.mediconnect.prescription.repository;

import com.mediconnect.prescription.model.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {
    List<Prescription> findByPatientId(String patientId);

    List<Prescription> findByDoctorId(String doctorId);

    List<Prescription> findByPharmacyId(String pharmacyId);

    List<Prescription> findByStatus(Prescription.PrescriptionStatus status);

    List<Prescription> findByPatientIdAndStatus(String patientId, Prescription.PrescriptionStatus status);

    List<Prescription> findByExpiresAtBeforeAndStatusNot(LocalDateTime date, Prescription.PrescriptionStatus status);
}
