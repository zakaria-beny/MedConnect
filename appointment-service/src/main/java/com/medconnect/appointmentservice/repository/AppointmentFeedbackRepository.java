package com.medconnect.appointmentservice.repository;

import com.medconnect.appointmentservice.model.AppointmentFeedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AppointmentFeedback documents.
 */
@Repository
public interface AppointmentFeedbackRepository extends MongoRepository<AppointmentFeedback, String> {
    List<AppointmentFeedback> findByDoctorId(String doctorId);
    Optional<AppointmentFeedback> findByAppointmentId(String appointmentId);
}
