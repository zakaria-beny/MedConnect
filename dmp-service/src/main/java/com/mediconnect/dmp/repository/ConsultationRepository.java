package com.mediconnect.dmp.repository;

import com.mediconnect.dmp.model.Consultation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationRepository extends MongoRepository<Consultation, String> {

    List<Consultation> findByPatientIdOrderByConsultationDateDesc(String patientId);

    List<Consultation> findByDoctorId(String doctorId);

    List<Consultation> findByPatientIdAndDoctorId(String patientId, String doctorId);

    void deleteByPatientId(String patientId);
}