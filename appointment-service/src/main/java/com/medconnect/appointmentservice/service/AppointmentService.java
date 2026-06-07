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
import com.medconnect.appointmentservice.model.AppointmentType;
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
    private final TeleconsultationClient teleconsultationClient;
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
        
        bookSlot(saved);

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

        releaseSlot(appointment);
        appointment.setDateTime(newDateTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setTeleconsultSessionId(null);
        appointment.setUpdatedAt(LocalDateTime.now());
        Appointment updated = appointmentRepository.save(appointment);
        bookSlot(updated);
        
        kafkaProducerService.publishAppointmentRescheduled(updated);
        log.info("Appointment rescheduled successfully: {}", id);
        return mapToResponse(updated);
    }

    public AppointmentResponse confirmAppointment(String id) {
        log.info("Confirming appointment: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.REJECTED) {
            throw new IllegalStateException("Cannot confirm appointment with status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        if (appointment.getType() == AppointmentType.VIDEO && appointment.getTeleconsultSessionId() == null) {
            teleconsultationClient.createSession(appointment)
                    .ifPresent(appointment::setTeleconsultSessionId);
        }
        appointment.setUpdatedAt(LocalDateTime.now());

        Appointment updated = appointmentRepository.save(appointment);
        kafkaProducerService.publishAppointmentConfirmed(updated);
        log.info("Appointment confirmed successfully: {}", id);
        return mapToResponse(updated);
    }

    public AppointmentResponse rejectAppointment(String id, String reason) {
        log.info("Rejecting appointment: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new IllegalStateException("Cannot reject appointment with status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointment.setCancellationReason(reason);
        appointment.setUpdatedAt(LocalDateTime.now());
        releaseSlot(appointment);

        Appointment updated = appointmentRepository.save(appointment);
        kafkaProducerService.publishAppointmentRejected(updated);
        log.info("Appointment rejected successfully: {}", id);
        return mapToResponse(updated);
    }

    public void cancelAppointment(String id, String reason) {
        log.info("Cancelling appointment: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found: " + id));
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);
        appointment.setUpdatedAt(LocalDateTime.now());
        releaseSlot(appointment);
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
        
        Optional<Appointment> existing = appointmentRepository.findByDoctorIdAndDateTimeAndStatusNotIn(
                doctorId, dateTime, List.of(AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED));
        
        if (existing.isPresent()) {
            throw new SlotAlreadyBookedException("Slot already booked for doctor: " + doctorId + " at " + dateTime);
        }
    }

    private void bookSlot(Appointment appointment) {
        String slotStartTime = appointment.getDateTime().toLocalTime().format(TIME_FORMATTER);
        slotRepository.findByDoctorIdAndDateAndStartTime(
                appointment.getDoctorId(),
                appointment.getDateTime().toLocalDate(),
                slotStartTime
        ).ifPresent(slot -> {
            slot.setBooked(true);
            slot.setAppointmentId(appointment.getId());
            slotRepository.save(slot);
        });
    }

    private void releaseSlot(Appointment appointment) {
        slotRepository.findByAppointmentId(appointment.getId()).ifPresent(slot -> {
            slot.setBooked(false);
            slot.setAppointmentId(null);
            slotRepository.save(slot);
        });
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
                .teleconsultSessionId(appointment.getTeleconsultSessionId())
                .createdAt(appointment.getCreatedAt().toString())
                .updatedAt(appointment.getUpdatedAt().toString())
                .build();
    }
}
