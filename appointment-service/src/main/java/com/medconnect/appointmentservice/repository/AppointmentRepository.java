package com.medconnect.appointmentservice.repository;

import com.medconnect.appointmentservice.model.Appointment;
import com.medconnect.appointmentservice.model.AppointmentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Appointment documents.
 */
@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByPatientIdOrderByDateTimeDesc(String patientId);
    List<Appointment> findByDoctorIdOrderByDateTimeAsc(String doctorId);
    List<Appointment> findByDoctorIdAndDateTimeBetween(String doctorId, LocalDateTime start, LocalDateTime end);
    Optional<Appointment> findByDoctorIdAndDateTimeAndStatusNot(String doctorId, LocalDateTime dateTime, AppointmentStatus status);
}
