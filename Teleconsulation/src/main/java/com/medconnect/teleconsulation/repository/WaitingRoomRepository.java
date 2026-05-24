package com.medconnect.teleconsulation.repository;

import com.medconnect.teleconsulation.model.WaitingRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WaitingRoomRepository extends MongoRepository<WaitingRoom, String> {
    List<WaitingRoom> findBySessionIdAndAdmittedFalseOrderByPositionAsc(String sessionId);
    List<WaitingRoom> findByDoctorIdAndAdmittedFalseOrderByPositionAsc(String doctorId);
    Optional<WaitingRoom> findBySessionIdAndPatientId(String sessionId, String patientId);
    Optional<WaitingRoom> findByPatientIdAndAdmittedFalse(String patientId);
    long countBySessionIdAndAdmittedFalse(String sessionId);
}
