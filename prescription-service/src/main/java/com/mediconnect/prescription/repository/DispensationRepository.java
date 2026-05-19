package com.mediconnect.prescription.repository;

import com.mediconnect.prescription.model.Dispensation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispensationRepository extends MongoRepository<Dispensation, String> {
    List<Dispensation> findByPrescriptionId(String prescriptionId);

    List<Dispensation> findByPharmacyId(String pharmacyId);

    List<Dispensation> findByPatientId(String patientId);
}
