package com.medconnect.appointmentservice.repository;

import com.medconnect.appointmentservice.model.WaitList;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for WaitList documents.
 */
@Repository
public interface WaitListRepository extends MongoRepository<WaitList, String> {
    List<WaitList> findByDoctorIdOrderByAddedAtAsc(String doctorId);
    Optional<WaitList> findByPatientIdAndDoctorId(String patientId, String doctorId);
}
