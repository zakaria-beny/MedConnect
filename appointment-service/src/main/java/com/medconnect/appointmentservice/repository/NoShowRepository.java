package com.medconnect.appointmentservice.repository;

import com.medconnect.appointmentservice.model.NoShow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for NoShow documents.
 */
@Repository
public interface NoShowRepository extends MongoRepository<NoShow, String> {
    List<NoShow> findByDoctorId(String doctorId);
    List<NoShow> findByPatientId(String patientId);
}
