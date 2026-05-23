package com.medconnect.appointmentservice.repository;

import com.medconnect.appointmentservice.model.CheckIn;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CheckIn documents.
 */
@Repository
public interface CheckInRepository extends MongoRepository<CheckIn, String> {
    Optional<CheckIn> findByAppointmentId(String appointmentId);
    List<CheckIn> findByDoctorIdOrderByQueuePositionAsc(String doctorId);
}
