package com.medconnect.appointmentservice.repository;

import com.medconnect.appointmentservice.model.DoctorSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for DoctorSchedule documents.
 */
@Repository
public interface DoctorScheduleRepository extends MongoRepository<DoctorSchedule, String> {
    Optional<DoctorSchedule> findByDoctorId(String doctorId);
}
