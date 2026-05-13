package com.medconnect.appointmentservice.service;

import com.medconnect.appointmentservice.exception.AppointmentNotFoundException;
import com.medconnect.appointmentservice.kafka.KafkaProducerService;
import com.medconnect.appointmentservice.model.AppointmentStatus;
import com.medconnect.appointmentservice.model.NoShow;
import com.medconnect.appointmentservice.model.Appointment;
import com.medconnect.appointmentservice.repository.NoShowRepository;
import com.medconnect.appointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing no-show records.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NoShowService {

    private final NoShowRepository noShowRepository;
    private final AppointmentRepository appointmentRepository;
    private final KafkaProducerService kafkaProducerService;

    public void markNoShow(String appointmentId) {
        log.info("Marking appointment as no-show: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + appointmentId));

        NoShow noShow = NoShow.builder()
                .id(UUID.randomUUID().toString())
                .appointmentId(appointmentId)
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .scheduledTime(appointment.getDateTime())
                .markedAt(LocalDateTime.now())
                .build();

        noShowRepository.save(noShow);

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepository.save(appointment);

        kafkaProducerService.publishNoShow(noShow);
        log.info("Appointment marked as no-show: {}", appointmentId);
    }

    public double getNoShowRateForDoctor(String doctorId) {
        log.info("Calculating no-show rate for doctor: {}", doctorId);
        
        List<NoShow> noShows = noShowRepository.findByDoctorId(doctorId);
        List<Appointment> allAppointments = appointmentRepository.findByDoctorIdOrderByDateTimeAsc(doctorId);

        if (allAppointments.isEmpty()) {
            return 0.0;
        }

        double rate = (double) noShows.size() / allAppointments.size() * 100;
        log.debug("No-show rate for doctor {}: {}%", doctorId, rate);
        return rate;
    }
}
