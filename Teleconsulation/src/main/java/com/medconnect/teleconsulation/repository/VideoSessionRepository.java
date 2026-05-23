package com.medconnect.teleconsulation.repository;

import com.medconnect.teleconsulation.model.VideoSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface VideoSessionRepository extends MongoRepository<VideoSession, String> {
    Optional<VideoSession> findBySessionId(String sessionId);
    List<VideoSession> findByDoctorId(String doctorId);
    List<VideoSession> findByPatientId(String patientId);
    Optional<VideoSession> findByAppointmentId(String appointmentId);
}
