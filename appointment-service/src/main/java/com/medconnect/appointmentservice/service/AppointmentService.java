package com.medconnect.appointmentservice.service;

import com.medconnect.appointmentservice.dto.request.BookAppointmentRequest;
import com.medconnect.appointmentservice.dto.request.RescheduleRequest;
import com.medconnect.appointmentservice.dto.response.AppointmentResponse;
import com.medconnect.appointmentservice.exception.AppointmentNotFoundException;
import com.medconnect.appointmentservice.exception.SlotAlreadyBookedException;
import com.medconnect.appointmentservice.kafka.KafkaProducerService;
import com.medconnect.appointmentservice.model.Appointment;
import com.medconnect.appointmentservice.model.AppointmentSlot;
import com.medconnect.appointmentservice.model.AppointmentStatus;
import com.medconnect.appointmentservice.repository.AppointmentRepository;
import com.medconnect.appointmentservice.repository.AppointmentSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing appointments.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository slotRepository;
    private final KafkaProducerService kafkaProducerService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public AppointmentResponse bookAppointment(BookAppointmentRequest request) {
        log.info("Booking appointment for patient: {}, doctor: {}", request.getPatientId(), request.getDoctorId());
        
        LocalDateTime dateTime = request.getDateTime();
        
        checkDoubleBooking(request.getDoctorId(), dateTime);

        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID().toString())
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .dateTime(dateTime)
                .type(request.getType())
                .status(AppointmentStatus.SCHEDULED)
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        
        // Mark slot as booked
        String slotStartTime = dateTime.toLocalTime().format(TIME_FORMATTER);
        List<AppointmentSlot> slots = slotRepository.findByDoctorIdAndDate(request.getDoctorId(), dateTime.toLocalDate());
        slots.stream()
                .filter(s -> s.getStartTime().equals(slotStartTime) && !s.isBooked())
                .forEach(s -> {
                    s.setBooked(true);
                    s.setAppointmentId(saved.getId());
                    slotRepository.save(s);
                });

        kafkaProducerService.publishAppointmentBooked(saved);
        log.info("Appointment booked successfully: {}", saved.getId());
        return mapToResponse(saved);
    }

    public AppointmentResponse getAppointment(String id) {
        log.info("Fetching appointment: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));
        
        return mapToResponse(appointment);
    }

    public AppointmentResponse rescheduleAppointment(String id, RescheduleRequest request) {
        log.info("Rescheduling appointment: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));
        
        LocalDateTime newDateTime = request.getNewDateTime();
        checkDoubleBooking(appointment.getDoctorId(), newDateTime);

        appointment.setDateTime(newDateTime);
        appointment.setUpdatedAt(LocalDateTime.now());
        Appointment updated = appointmentRepository.save(appointment);
        
        kafkaProducerService.publishAppointmentRescheduled(updated);
        log.info("Appointment rescheduled successfully: {}", id);
        return mapToResponse(updated);
    }

    public void cancelAppointment(String id, String reason) {
        log.info("Cancelling appointment: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);
        appointment.setUpdatedAt(LocalDateTime.now());
        appointmentRepository.save(appointment);
        
        kafkaProducerService.publishAppointmentCancelled(appointment);
        log.info("Appointment cancelled successfully: {}", id);
    }

    public List<AppointmentResponse> getPatientAppointments(String patientId) {
        log.info("Fetching appointments for patient: {}", patientId);
        
        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByDateTimeDesc(patientId);
        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getDoctorAppointments(String doctorId) {
        log.info("Fetching appointments for doctor: {}", doctorId);
        
        List<Appointment> appointments = appointmentRepository.findByDoctorIdOrderByDateTimeAsc(doctorId);
        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void checkDoubleBooking(String doctorId, LocalDateTime dateTime) {
        log.debug("Checking for double booking: doctor {}, dateTime {}", doctorId, dateTime);
        
        Optional<Appointment> existing = appointmentRepository.findByDoctorIdAndDateTimeAndStatusNot(
                doctorId, dateTime, AppointmentStatus.CANCELLED);
        
        if (existing.isPresent()) {
            throw new SlotAlreadyBookedException("Slot already booked for doctor: " + doctorId + " at " + dateTime);
        }
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .dateTime(appointment.getDateTime().toString())
                .formattedDateTime(appointment.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .type(appointment.getType())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .cancellationReason(appointment.getCancellationReason())
                .createdAt(appointment.getCreatedAt().toString())
                .updatedAt(appointment.getUpdatedAt().toString())
                .build();
    }
}
