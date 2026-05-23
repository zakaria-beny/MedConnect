package com.medconnect.appointmentservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medconnect.appointmentservice.model.Appointment;
import com.medconnect.appointmentservice.model.NoShow;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * Kafka producer service for publishing appointment events.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishAppointmentBooked(Appointment appointment) {
        try {
            String message = objectMapper.writeValueAsString(appointment);
            kafkaTemplate.send("appointment.booked", appointment.getId(), message);
            log.info("Published appointment.booked event for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error publishing appointment.booked event", e);
        }
    }

    public void publishAppointmentCancelled(Appointment appointment) {
        try {
            String message = objectMapper.writeValueAsString(appointment);
            kafkaTemplate.send("appointment.cancelled", appointment.getId(), message);
            log.info("Published appointment.cancelled event for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error publishing appointment.cancelled event", e);
        }
    }

    public void publishAppointmentRescheduled(Appointment appointment) {
        try {
            String message = objectMapper.writeValueAsString(appointment);
            kafkaTemplate.send("appointment.rescheduled", appointment.getId(), message);
            log.info("Published appointment.rescheduled event for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error publishing appointment.rescheduled event", e);
        }
    }

    public void publishNoShow(NoShow noShow) {
        try {
            String message = objectMapper.writeValueAsString(noShow);
            kafkaTemplate.send("appointment.no_show", noShow.getId(), message);
            log.info("Published appointment.no_show event for appointment: {}", noShow.getAppointmentId());
        } catch (Exception e) {
            log.error("Error publishing appointment.no_show event", e);
        }
    }
}
