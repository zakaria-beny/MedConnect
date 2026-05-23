package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.Vaccination;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccinationRepository extends MongoRepository<Vaccination, String> {

    List<Vaccination> findByPatientIdOrderByAdministrationDateDesc(String patientId);

    // Find vaccinations where next dose is due soon (useful for reminders)
    List<Vaccination> findByPatientIdAndNextDoseDateBefore(String patientId, LocalDate date);

    void deleteByPatientId(String patientId);
}