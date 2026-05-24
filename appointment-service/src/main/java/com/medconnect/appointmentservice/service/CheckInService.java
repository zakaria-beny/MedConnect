package com.medconnect.appointmentservice.service;

import com.medconnect.appointmentservice.dto.response.CheckInResponse;
import com.medconnect.appointmentservice.exception.AppointmentNotFoundException;
import com.medconnect.appointmentservice.model.Appointment;
import com.medconnect.appointmentservice.model.CheckIn;
import com.medconnect.appointmentservice.repository.CheckInRepository;
import com.medconnect.appointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing patient check-ins.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final AppointmentRepository appointmentRepository;

    public CheckInResponse checkInPatient(String appointmentId) {
        log.info("Checking in patient for appointment: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));

        long queueCount = checkInRepository.findAll().stream()
                .filter(c -> c.getCheckInTime().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();

        CheckIn checkIn = CheckIn.builder()
                .id(UUID.randomUUID().toString())
                .appointmentId(appointmentId)
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .checkInTime(LocalDateTime.now())
                .queuePosition((int) (queueCount + 1))
                .estimatedWaitMinutes((int) (queueCount * 15))
                .build();

        CheckIn saved = checkInRepository.save(checkIn);
        log.info("Patient checked in successfully: {}", appointmentId);
        return mapToResponse(saved);
    }

    public CheckInResponse getQueuePosition(String appointmentId) {
        log.info("Fetching queue position for appointment: {}", appointmentId);
        
        CheckIn checkIn = checkInRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Check-in not found for appointment: " + appointmentId));
        
        return mapToResponse(checkIn);
    }

    public int estimateWaitTime(String appointmentId, int appointmentDurationMinutes) {
        log.info("Estimating wait time for appointment: {}", appointmentId);
        
        CheckIn checkIn = checkInRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Check-in not found for appointment: " + appointmentId));
        
        return checkIn.getQueuePosition() * appointmentDurationMinutes;
    }

    private CheckInResponse mapToResponse(CheckIn checkIn) {
        return CheckInResponse.builder()
                .appointmentId(checkIn.getAppointmentId())
                .queuePosition(checkIn.getQueuePosition())
                .estimatedWaitMinutes(checkIn.getEstimatedWaitMinutes())
                .build();
    }
}
