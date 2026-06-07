package com.medconnect.appointmentservice.repository;

import com.medconnect.appointmentservice.model.AppointmentSlot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for AppointmentSlot documents.
 */
@Repository
public interface AppointmentSlotRepository extends MongoRepository<AppointmentSlot, String> {
    List<AppointmentSlot> findByDoctorIdAndDate(String doctorId, LocalDate date);
    List<AppointmentSlot> findByDoctorIdAndDateAndBookedFalse(String doctorId, LocalDate date);
    Optional<AppointmentSlot> findByDoctorIdAndDateAndStartTime(String doctorId, LocalDate date, String startTime);
    Optional<AppointmentSlot> findByAppointmentId(String appointmentId);
}
