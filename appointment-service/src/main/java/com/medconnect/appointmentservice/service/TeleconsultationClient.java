package com.medconnect.appointmentservice.service;

import com.medconnect.appointmentservice.model.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class TeleconsultationClient {

    @Value("${medconnect.teleconsultation.base-url:http://teleconsultation-service:8086/api/teleconsult}")
    private String teleconsultationBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<String> createSession(Appointment appointment) {
        Map<String, String> payload = Map.of(
                "appointmentId", appointment.getId(),
                "doctorId", appointment.getDoctorId(),
                "patientId", appointment.getPatientId()
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    teleconsultationBaseUrl + "/sessions",
                    payload,
                    Map.class
            );

            if (response == null) {
                return Optional.empty();
            }

            Object sessionId = response.get("sessionId");
            if (sessionId == null) {
                sessionId = response.get("id");
            }
            return sessionId == null ? Optional.empty() : Optional.of(sessionId.toString());
        } catch (RestClientException e) {
            log.warn("Could not create teleconsultation session for appointment {}", appointment.getId(), e);
            return Optional.empty();
        }
    }
}
