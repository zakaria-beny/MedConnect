// =============================================
// FILE: repository/AllergyRepository.java
// =============================================
package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.Allergy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * WHY MongoRepository?
 * MongoRepository<Allergy, String> means:
 *   - Allergy = the document type this repository manages
 *   - String  = the type of the @Id field
 *
 * Spring Data AUTOMATICALLY implements these methods for us:
 *   - save(allergy)
 *   - findById(id)
 *   - findAll()
 *   - deleteById(id)
 *   - count()
 *   - existsById(id)
 *
 * We only need to DECLARE custom methods here — Spring generates the SQL/MongoDB query from the method name!
 * Example: findByPatientId → Spring creates: db.allergies.find({ patientId: "..." })
 */
@Repository
public interface AllergyRepository extends MongoRepository<Allergy, String> {

    // Find all allergies for a patient
    List<Allergy> findByPatientId(String patientId);

    // Find only active allergies for a patient
    List<Allergy> findByPatientIdAndActiveTrue(String patientId);

    // Find by patient and severity (e.g., find all LIFE_THREATENING allergies)
    List<Allergy> findByPatientIdAndSeverity(String patientId, Allergy.SeverityLevel severity);

    // Check if a patient is allergic to a specific substance
    Optional<Allergy> findByPatientIdAndAllergen(String patientId, String allergen);

    // Check if an allergen already exists for a patient (avoid duplicates)
    boolean existsByPatientIdAndAllergen(String patientId, String allergen);

    // Check if an active allergen exists for a patient
    boolean existsByPatientIdAndAllergenAndActiveTrue(String patientId, String allergen);

    // Delete all allergies for a patient (used when deleting patient data)
    void deleteByPatientId(String patientId);
}